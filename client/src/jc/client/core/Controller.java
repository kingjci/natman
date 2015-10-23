package jc.client.core;

import jc.Random;
import jc.TCPConnection;
import jc.command.Command;
import jc.command.QuitCommand;
import jc.message.*;
import jc.Time;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;

import static jc.Utils.*;

public class Controller implements Runnable {

    private String clientId;
    private Map<String, PrivateTunnel> privateTunnels;
    private Map<String, PublicTunnelConfiguration> publicTunnelConfiguration;
    private Time lastPing;
    private Time lastPingResponse;
    private Random random;
    private Option option;
    private Config config;
    private TCPConnection tcpConnection;
    private BlockingQueue<Command> commands;
    private Logger runtimeLogger;
    private Logger accessLogger;

    private Timer heartBeat;
    private Timer heartBeatResponseCheck;

    public Controller(
            Config config,
            Option option,
            Random random,
            BlockingQueue<Command> commands,
            Logger runtimeLogger,
            Logger accessLogger
    ){

        this.config = config;
        this.option = option;
        this.privateTunnels = new HashMap<>();
        this.publicTunnelConfiguration = config.getPublicTunnelConfigurations();
        this.lastPing = new Time();
        this.lastPingResponse = new Time();
        this.random = random;
        this.commands = commands;
        this.runtimeLogger = runtimeLogger;
        this.accessLogger = accessLogger;
    }

    public String getClientId() {
        return clientId;
    }
    public Logger getRuntimeLogger() {
        return runtimeLogger;
    }
    public TCPConnection getTcpConnection() {
        return tcpConnection;
    }
    public Config getConfig() {
        return config;
    }
    public Option getOption() {
        return option;
    }
    public Random getRandom() {
        return random;
    }
    public Time getLastPingResponse() {
        return lastPingResponse;
    }
    public Time getLastPing() {
        return lastPing;
    }
    public BlockingQueue<Command> getCommands() {
        return commands;
    }
    public Map<String, PrivateTunnel> getPrivateTunnels() {
        return privateTunnels;
    }
    public Logger getAccessLogger() {
        return accessLogger;
    }


    @Override
    public void run() {

        int waitTime = option.getWaitTime();
        int maxWaitCount = option.getMaxWaitCount();
        int waitCount = 0;

        while (true){

            control();

            try{
                Thread.sleep(waitTime);
                waitCount++;
                runtimeLogger.info(
                        String.format(
                                "Reconnect to %s for %d times",
                                config.getServerAddress(),
                                waitCount
                        )
                );
                if (waitCount == maxWaitCount){
                    runtimeLogger.info(
                            String.format(
                                    "Fail to reconnect to %s for %d times",
                                    config.getServerAddress(),
                                    option.getWaitTime()
                            )
                    );
                    break;
                }
            }catch (InterruptedException e){
                runtimeLogger.error(e.getMessage(),e);
            }
        }

        QuitCommand quitCommand = new QuitCommand("controller", "Disconnect from the server",-10);
        try{
            commands.put(quitCommand);
        }catch (InterruptedException e){
            runtimeLogger.error(e.getMessage(),e);
        }
    }

    public void control(){

        tcpConnection = Dial(
                        config.getServerAddress(),
                        config.getControlPort(),
                        "control",
                        random.getRandomString(8),
                        runtimeLogger,
                        accessLogger
                );

        if (tcpConnection == null){
            return;
        }

        AuthRequest authRequest = new AuthRequest(clientId, 1.0f);

        if (config.getUsername() != null && !"".equals(config.getUsername())){
            authRequest.setUsername(config.getUsername());
        }

        if (config.getPassword() != null && !"".equals(config.getPassword())){
            authRequest.setPassword(config.getPassword());
        }

        AuthResponse authResponse = null;

        try{
            tcpConnection.writeMessage(authRequest);
            authResponse =(AuthResponse) tcpConnection.readMessage();
        }catch (IOException e){
            runtimeLogger.error(
                    String.format(
                            "Fail to communicate with server[%s]",
                            tcpConnection.getRemoteAddress()
                    )
            );
           System.exit(-1);
        }

        if (authResponse == null){
            runtimeLogger.error("Receive null AuthResponse");
            System.exit(-1);
        }

        if (authResponse.hasError()){
            accessLogger.error(
                    String.format(
                            "Refused by server[%s] with username[%s] password[%s]",
                            config.getServerAddress(),
                            config.getUsername(),
                            config.getPassword()
                    )
            );
            System.exit(-1);
        }

        clientId = authResponse.getClientId();
        accessLogger.info(String.format(
                "Authenticated with server[%s], client id: %s",
                config.getServerAddress(),
                clientId
            )
        );

        for (Map.Entry<String, PublicTunnelConfiguration> entry : publicTunnelConfiguration.entrySet()){

            PublicTunnelConfiguration publicTunnelConfiguration = entry.getValue();
            PublicTunnelRequest publicTunnelRequest =
                    new PublicTunnelRequest(
                            clientId ,
                            publicTunnelConfiguration.getProtocol(),
                            publicTunnelConfiguration.getSubDomain(),
                            publicTunnelConfiguration.getRemotePort(),
                            publicTunnelConfiguration.getLocalPort()
                    );

            try {
                tcpConnection.writeMessage(publicTunnelRequest);

                PublicTunnelResponse publicTunnelResponse =
                        (PublicTunnelResponse) tcpConnection.readMessage();

                if (publicTunnelResponse.hasError()) {
                    runtimeLogger.error(
                            String.format(
                                    "Server fails to allocate tunnel, %s",
                                    publicTunnelResponse.getError()
                            )
                    );

                    System.exit(-1);
                }

                if (publicTunnelResponse.getRemotePort() != publicTunnelRequest.getRemotePort()){
                    runtimeLogger.warn(
                            String.format(
                                    "%d port on server[%s] is occupied. get random port %d",
                                    publicTunnelRequest.getRemotePort(),
                                    config.getServerAddress(),
                                    publicTunnelResponse.getRemotePort()
                            )
                    );
                }

                PrivateTunnel privateTunnel =
                        new PrivateTunnel(
                                publicTunnelResponse.getPublicUrl(),
                                config.getClientAddress(),
                                publicTunnelResponse.getLocalPort(),
                                publicTunnelResponse.getProtocol()
                        );

                privateTunnels.put(privateTunnel.getPublicUrl(), privateTunnel);
                runtimeLogger.info(
                        String.format(
                                "PrivateTunnel established at %s successfully",
                                privateTunnel.getPublicUrl()
                        )
                );

            }catch (IOException e){
                runtimeLogger.error(
                        String.format(
                                "Control connection is closed: can not write to %s",
                                tcpConnection.getRemoteAddress()
                        )
                );
                return;
            }
        }


        lastPingResponse.setTime(System.currentTimeMillis());

        if (heartBeat != null){
            heartBeat.cancel();
        }
        heartBeat = new Timer();
        heartBeat.schedule(new ControllerHeartBeatTask(this), 0, option.getHeartBeatInterval());

        if ((heartBeatResponseCheck != null)){
            heartBeatResponseCheck.cancel();
        }
        heartBeatResponseCheck = new Timer();
        heartBeatResponseCheck.schedule(new ControllerHeartBeatCheckerTask(this), 0, option.getHeartBeatCheckerInterval());

        while (true){

            Message message;
            try{
                message = tcpConnection.readMessage();
            }catch (IOException e){
                runtimeLogger.error(
                        String.format(
                            "Control connection is closed: can not read from %s",
                            tcpConnection.getRemoteAddress()
                        )
                );
                return;
            }

            switch (message.getMessageType()){

                case "ProxyRequest":

                    Go(new Proxy(this));

                    break;

                case "PingResponse":

                    lastPingResponse.setTime(System.currentTimeMillis());
                    break;

                case "PublicTunnelResponse":

                    PublicTunnelResponse publicTunnelResponse = (PublicTunnelResponse) message;
                    if (publicTunnelResponse.hasError()){
                        runtimeLogger.error(
                                String.format(
                                        "Server fails to allocate tunnel, %s",
                                        publicTunnelResponse.getError()
                                )
                        );

                        shutDown(publicTunnelResponse.getError(), -2);
                        return;
                    }

                    PrivateTunnel privateTunnel =
                            new PrivateTunnel(
                                    publicTunnelResponse.getPublicUrl(),
                                    config.getClientAddress(),
                                    publicTunnelResponse.getLocalPort(),
                                    publicTunnelResponse.getProtocol()
                            );

                    privateTunnels.put(privateTunnel.getPublicUrl(), privateTunnel);
                    runtimeLogger.info(
                            String.format(
                                 "PrivateTunnel established at %s successfully",
                                  privateTunnel.getPublicUrl()
                            )
                    );

                    break;

                default:
                    runtimeLogger.error("Ignoring unknown control message");
            }
        }

    }

    public void shutDown(String reason, int exitCode){

        try{
            QuitCommand quitCommand = new QuitCommand("Controller" ,reason, exitCode);
            commands.put(quitCommand);
            tcpConnection.close();
        }catch (IOException|InterruptedException e){
            runtimeLogger.error(e.getMessage(),e);
        }

    }
}

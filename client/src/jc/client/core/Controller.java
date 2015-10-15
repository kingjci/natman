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
import java.util.concurrent.BlockingQueue;

import static jc.Utils.*;

public class Controller implements Runnable {

    private String clientId;
    private String serverAddress;
    private Map<String, PrivateTunnel> privateTunnels;
    private Map<String, PublicTunnelConfiguration> publicTunnelConfiguration;
    private Time lastPingResponse;
    private Random random;
    private Option option;
    private Config config;
    private TCPConnection tcpConnection;
    private BlockingQueue<Command> commands;
    private Logger runtimeLogger;
    private Logger accessLogger;

    public Controller(
            Config config,
            Random random,
            BlockingQueue<Command> commands,
            Logger runtimeLogger,
            Logger accessLogger
    ){

        this.serverAddress = config.getServerAddress();
        this.privateTunnels = new HashMap<>();
        this.publicTunnelConfiguration = config.getPublicTunnelConfigurations();
        this.lastPingResponse = new Time();
        this.random = random;
        this.commands = commands;
        this.runtimeLogger = runtimeLogger;
        this.accessLogger = accessLogger;
    }

    public String getClientId() {
        return clientId;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public Logger getAccessLogger() {
        return accessLogger;
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

    public Map<String, PublicTunnelConfiguration> getPublicTunnelConfiguration() {
        return publicTunnelConfiguration;
    }

    public Map<String, PrivateTunnel> getPrivateTunnels() {
        return privateTunnels;
    }

    public BlockingQueue<Command> getCommands() {
        return commands;
    }

    @Override
    public void run() {

        int wait = option.getWaitTime();

        while (true){

            control();

            try{
                Thread.sleep(wait);
            }catch (InterruptedException e){
                runtimeLogger.error(e.getMessage(),e);
            }
        }

    }

    public void control(){

        tcpConnection = Dial(
                        serverAddress,
                        option.getControlPort(),
                        "control",
                        random.getRandomString(8),
                        runtimeLogger,
                        accessLogger
                );

        AuthRequest authRequest = new AuthRequest(clientId, 1.0f);
        AuthResponse authResponse = null;

        try{
            tcpConnection.writeMessage(authRequest);
            authResponse =(AuthResponse) tcpConnection.readMessage();
        }catch (IOException e){
            runtimeLogger.error(e.getMessage(), e);
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
                            publicTunnelConfiguration.getRemotePort(),
                            publicTunnelConfiguration.getLocalPort()
                    );

            try {
                tcpConnection.writeMessage(publicTunnelRequest);
            }catch (IOException e){
                e.printStackTrace();
            }

        }

        this.lastPingResponse.setTime(System.currentTimeMillis());

        Go(new ControllerHeartBeat(this));

        while (true){

            Message message = null;

            try{
                message = tcpConnection.readMessage();
            }catch (IOException e){
                runtimeLogger.error("Control connection is closed,prepare to exit");
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

    public PrivateTunnel getPrivateTunnel(String privateTunnel){
        return privateTunnels.get(privateTunnel);
    }

    public void shutDown(String reason, int exitCode){

        tcpConnection.close();
        QuitCommand quitCommand = new QuitCommand("Controller" ,reason, exitCode);
        try{
            commands.put(quitCommand);
        }catch (InterruptedException e){
            runtimeLogger.error(e.getMessage(),e);
        }

    }
}

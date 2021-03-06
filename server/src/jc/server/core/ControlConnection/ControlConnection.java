package jc.server.core.ControlConnection;

import jc.Random;
import jc.message.*;
import jc.server.core.Config;
import jc.server.core.Controller.ControllerHandler;
import jc.server.core.Option;
import jc.TCPConnection;
import jc.Time;
import jc.server.core.PublicTunnel.TCP.PublicTCPTunnelRegistry;
import jc.server.core.PublicTunnel.Result.PublicTunnelRegisterResult;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ControlConnection extends Thread{

    private final TCPConnection tcpConnection;
    private final Time lastPing;
    private final BlockingQueue<TCPConnection> proxies;
    private final String clientId;
    private final Timer pingChecker;
    private final Random random;
    private final ControlConnectionRegistry controlConnectionRegistry;
    private final PublicTCPTunnelRegistry publicTCPTunnelRegistry;
    private final Logger runtimeLogger;
    private final Logger accessLogger;
    private final Config config;
    private final Option option;

    public ControlConnection(
            ControllerHandler controllerHandler,
            String clientId
    ){

        this.clientId = clientId;
        this.tcpConnection = controllerHandler.getTcpConnection();
        this.controlConnectionRegistry = controllerHandler.getControlConnectionRegistry();
        this.publicTCPTunnelRegistry = controllerHandler.getPublicTCPTunnelRegistry();
        this.random = controllerHandler.getRandom();
        this.runtimeLogger = controllerHandler.getRuntimeLogger();
        this.accessLogger = controllerHandler.getAccessLogger();
        this.config = controllerHandler.getConfig();
        this.option = controllerHandler.getOption();


        this.tcpConnection.setType("control");
        this.proxies = new LinkedBlockingQueue<>();
        this.pingChecker = new Timer();
        this.lastPing = new Time(System.currentTimeMillis());

    }

    public TCPConnection getProxy(){

        TCPConnection proxyTCPConnection = null;

        try{
            tcpConnection.writeMessage(new ProxyRequest());
            proxyTCPConnection = proxies.poll(option.getMaxGetProxyTime(), TimeUnit.SECONDS);
        }catch (IOException|InterruptedException e){
            runtimeLogger.error(e.getMessage(),e);
        }

        if (proxyTCPConnection == null){
            runtimeLogger.error(
                    String.format(
                            "Fail to get proxy from %s[%s]",
                            tcpConnection.getRemoteAddress(),
                            clientId
                    )
            );
        }

        return proxyTCPConnection;

    }

    public void putProxy(TCPConnection tcpConnection){

        try{
            proxies.put(tcpConnection);
        }catch (InterruptedException e){
            runtimeLogger.error(e.getMessage(),e);
        }

    }

    public TCPConnection getTcpConnection() {
        return tcpConnection;
    }
    public Time getLastPing() {
        return lastPing;
    }
    public Logger getRuntimeLogger() {
        return runtimeLogger;
    }
    public Logger getAccessLogger() {
        return accessLogger;
    }
    public String getClientId() {
        return clientId;
    }
    public String getConnectionId(){
        return tcpConnection.getConnectionId();
    }
    public String getRemoteAddress() {
        return tcpConnection.getRemoteAddress();
    }
    public void close() throws IOException{
        //   when the run() is blocked in Message message = tcpConnection.readMessage();
        //this operation will cause exception in run()
        tcpConnection.close();
        accessLogger.info(
                String.format(
                        "Control connection from %s[%s] is closed",
                        tcpConnection.getRemoteAddress(),
                        clientId
                )
        );
        pingChecker.cancel();
        runtimeLogger.debug(
                String.format(
                        "Heartbeat of %s[%s] is closed",
                        tcpConnection.getRemoteAddress(),
                        clientId
                )
        );
    }

    @Override
    public void run() {

        pingChecker.schedule(new ControlConnectionHeartBeatChecker(this), 0, 10*1000);

        while (true){

            try{
                Message message = tcpConnection.readMessage();
                switch (message.getMessageType()){

                    case "PublicTunnelRequest":

                        PublicTunnelRequest publicTunnelRequest = (PublicTunnelRequest) message;

                        accessLogger.info(
                            String.format(
                                "Client %s[%s] is requesting %s on port %d",
                                    tcpConnection.getRemoteAddress(),
                                    clientId,
                                    publicTunnelRequest.getProtocol(),
                                    publicTunnelRequest.getRemotePort()
                            )
                        );


                        PublicTunnelRegisterResult result = null;
                        switch (publicTunnelRequest.getProtocol()){


                            case "tcp":

                                result =
                                        publicTCPTunnelRegistry.register(
                                                publicTunnelRequest.getRemotePort(),
                                                this
                                        );

                                break;


                            case "http":



                                break;


                            default:

                        }

                        PublicTunnelResponse publicTunnelResponse;
                        if ("success".equalsIgnoreCase(result.getState())){
                            //  bind public tunnel successfully

                            publicTunnelResponse =
                                    new PublicTunnelResponse(
                                            result.getPublicUrl(),
                                            publicTunnelRequest.getProtocol(),
                                            publicTunnelRequest.getLocalPort(),
                                            result.getRemotePort()
                                    );

                        }else {

                            publicTunnelResponse = new PublicTunnelResponse(result.getError());

                            try{
                                close();
                            }catch (IOException e){
                                accessLogger.info(
                                        String.format("Fail to close control connection[%s] from %s[%s]",
                                                tcpConnection.getConnectionId(),
                                                tcpConnection.getRemoteAddress(),
                                                clientId
                                        )
                                );
                            }
                        }



                        try{
                            tcpConnection.writeMessage(publicTunnelResponse);
                        }catch (IOException e){
                            accessLogger.info(
                                    String.format("Send PublicTunnelResponse to %s failure",
                                            tcpConnection.getRemoteAddress()
                                    )
                            );
                        }




                        break;

                    case "PingRequest":

                        lastPing.setTime(System.currentTimeMillis());
                        PingResponse pingResponse = new PingResponse();
                        runtimeLogger.debug(
                                String.format(
                                        "Ping from %s[%s][%s] successfully",
                                        tcpConnection.getRemoteAddress(),
                                        clientId,
                                        tcpConnection.getConnectionId()
                                )
                        );
                        try{
                            tcpConnection.writeMessage(pingResponse);
                        }catch (IOException e){
                            runtimeLogger.error(e.getMessage(),e);
                        }
                        break;

                    default:

                        accessLogger.info(
                                String.format("Unknown message from %s[%s]",
                                        tcpConnection.getRemoteAddress(),
                                        tcpConnection.getConnectionId()
                                )
                        );
                       break;
                }

            }catch (IOException e){
                //   when close function of tcpConnection is called, program will run to here
                //it can be closed by the close function or another side of the client


                //  clean public tunnels associate with the control connection and delete
                //the control connection from controlConnectionRegistry
                pingChecker.cancel();
                publicTCPTunnelRegistry.delete(clientId);
                controlConnectionRegistry.delete(clientId);
                runtimeLogger.info(
                        String.format("Control connection[%s] to %s is closed",
                                clientId,
                                tcpConnection.getRemoteAddress()
                        )
                );
                return;
            }

        }
    }
}
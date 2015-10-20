package jc.server.core.ControlConnection;

import jc.Random;
import jc.message.*;
import jc.server.core.Config;
import jc.server.core.Controller.ControllerHandler;
import jc.server.core.Option;
import jc.server.core.PublicTunnel.PublicTunnel;
import jc.TCPConnection;
import jc.Time;
import jc.server.core.PublicTunnel.PublicTunnelRegistry;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static jc.Utils.Go;

public class ControlConnection extends Thread{

    private final TCPConnection tcpConnection;
    private final Time lastPing;
    private final BlockingQueue<TCPConnection> proxies;
    private final String clientId;
    private final Timer pingChecker;
    private final Random random;
    private final ControlConnectionRegistry controlConnectionRegistry;
    private final PublicTunnelRegistry publicTunnelRegistry;
    private final Logger runtimeLogger;
    private final Logger accessLogger;
    private final Config config;
    private final Option option;

    public ControlConnection(
            ControllerHandler controllerHandler
    ){

        this.clientId = controllerHandler.getClientId();
        this.tcpConnection = controllerHandler.getTcpConnection();
        this.controlConnectionRegistry = controllerHandler.getControlConnectionRegistry();
        this.publicTunnelRegistry = controllerHandler.getPublicTunnelRegistry();
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
            runtimeLogger.error("Get null proxy from client");
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
        accessLogger.info(
                String.format(
                        "Control connection from %s[%s] is closed",
                        tcpConnection.getRemoteAddress(),
                        clientId
                )
        );
        tcpConnection.close();
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
                        String publicUrl =
                                String.format("%s://%s:%d",
                                        publicTunnelRequest.getProtocol(),
                                        config.getDomain(),
                                        publicTunnelRequest.getRemotePort()
                                );
                        accessLogger.info(
                            String.format(
                                "Client %s[%s] is requesting %s",
                                    tcpConnection.getRemoteAddress(),
                                    clientId,
                                    publicUrl
                            )
                        );

                        PublicTunnel publicTunnel =
                                new PublicTunnel(publicTunnelRequest,
                                        publicUrl,
                                        this,
                                        random,
                                        runtimeLogger,
                                        accessLogger);
                        String result = null;
                        result = publicTunnelRegistry.register(clientId, publicTunnel);

                        if (!"success".equalsIgnoreCase(result)){

                            PublicTunnelResponse publicTunnelResponse =
                                    new PublicTunnelResponse(result);

                            try{
                                tcpConnection.writeMessage(publicTunnelResponse);
                            }catch (IOException e){
                                accessLogger.info(
                                        String.format("Send PublicTunnelResponse to %s failure",
                                                tcpConnection.getRemoteAddress()
                                        )
                                );
                            }

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

                        result = publicTunnel.bind(publicTunnelRequest.getRemotePort());
                        if (!"success".equalsIgnoreCase(result)){

                            PublicTunnelResponse publicTunnelResponse =
                                    new PublicTunnelResponse(result);

                            try{
                                tcpConnection.writeMessage(publicTunnelResponse);
                            }catch (IOException e){
                                accessLogger.info(
                                        String.format("Send PublicTunnelResponse to %s failure",
                                                tcpConnection.getRemoteAddress()
                                        )
                                );
                            }

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

                        }else {
                            //  bind public tunnel successfully
                            PublicTunnelResponse publicTunnelResponse =
                                    new PublicTunnelResponse(publicUrl,
                                            publicTunnelRequest.getProtocol(),
                                            publicTunnelRequest.getLocalPort()
                                    );
                            try{
                                tcpConnection.writeMessage(publicTunnelResponse);
                            }catch (IOException e){
                                accessLogger.info(
                                        String.format("Send PublicTunnelResponse to %s failure",
                                                tcpConnection.getRemoteAddress()
                                        )
                                );
                            }
                        }

                        Go(publicTunnel);

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
                publicTunnelRegistry.delete(clientId);
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
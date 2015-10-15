package jc.server.core.Controller;

import jc.Random;
import jc.Version;
import jc.message.AuthRequest;
import jc.message.AuthResponse;
import jc.message.Message;
import jc.TCPConnection;
import jc.message.ProxyResponse;
import jc.server.core.Config;
import jc.server.core.ControlConnection.ControlConnection;
import jc.server.core.ControlConnection.ControlConnectionRegistry;
import jc.server.core.Option;
import jc.server.core.PublicTunnel.PublicTunnelRegistry;
import org.apache.log4j.Logger;

import java.io.IOException;

public class ControllerHandler implements Runnable {

    private final TCPConnection tcpConnection;
    private final ControlConnectionRegistry controlConnectionRegistry;
    private final PublicTunnelRegistry publicTunnelRegistry;
    private final Random random;
    private final Config config;
    private final Option option;
    private final Logger runtimeLogger;
    private final Logger accessLogger;
    private String clientId;

    public ControllerHandler(
            TCPConnection tcpConnection,
            Controller controller
    ) {
        this.tcpConnection = tcpConnection;
        this.controlConnectionRegistry = controller.getControlConnectionRegistry();
        this.publicTunnelRegistry = controller.getPublicTunnelRegistry();
        this.random = controller.getRandom();
        this.config = controller.getConfig();
        this.option = controller.getOption();
        this.runtimeLogger = controller.getRuntimeLogger();
        this.accessLogger = controller.getAccessLogger();
    }

    public TCPConnection getTcpConnection() {
        return tcpConnection;
    }

    public ControlConnectionRegistry getControlConnectionRegistry() {
        return controlConnectionRegistry;
    }

    public PublicTunnelRegistry getPublicTunnelRegistry() {
        return publicTunnelRegistry;
    }

    public Random getRandom() {
        return random;
    }

    public Config getConfig() {
        return config;
    }

    public Option getOption() {
        return option;
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

    @Override
    public void run() {

            Message message = null;
            try {
                message = tcpConnection.readMessage();
            }catch (IOException e){
                runtimeLogger.error(e.getMessage(), e);
            }

            ControlConnection controlConnection;

            switch (message.getMessageType()) {

                case "AuthRequest":

                    AuthRequest authRequest = (AuthRequest) message;
                    AuthResponse authResponse = new AuthResponse(Version.Current);
                    //here is the auth process





                    if (authRequest.getVersion() < option.getMinVersion()){

                        String error = String.format(
                                "Incompatible versions. Server current/min version is %f/%f, client %s. Download a new version",
                                option.getVersion(),
                                option.getMinVersion(),
                                authRequest.getVersion()
                        );

                        authResponse.setError(error);
                        try{
                            tcpConnection.writeMessage(authResponse);
                        }catch (IOException e){

                            runtimeLogger.error(
                                    String.format("Send AuthResponse to %s failure",
                                            tcpConnection.getRemoteAddress()
                                    ),e
                            );

                        }

                        //  auth fails, terminate the auth prematurely
                        return;
                    }


                    if (authRequest.isNew()){
                        clientId = random.getRandomString(16);

                    }else {
                        clientId = authRequest.getClientId();
                    }
                    authResponse.setClientId(clientId);

                    accessLogger.info(
                            String.format("auth client %s[%s] successfully",
                                    tcpConnection.getRemoteAddress(),
                                    authResponse.getClientId()
                            )
                    );

                    try{
                        this.tcpConnection.writeMessage(authResponse);
                    }catch (IOException e){
                        runtimeLogger.error(
                                String.format("Send AuthResponse to %s failure",
                                        tcpConnection.getRemoteAddress()
                                ),e
                        );
                    }


                    controlConnection = new ControlConnection(this);

                    controlConnectionRegistry.register(controlConnection);
                    accessLogger.info(String.format("register control connection[%s] from %s[%s]",
                                    controlConnection.getConnectionId(),
                                    controlConnection.getRemoteAddress(),
                                    controlConnection.getClientId())
                    );

                    controlConnection.start();

                    break;

                case "ProxyResponse":{

                    ProxyResponse proxyResponse = (ProxyResponse) message;

                    tcpConnection.setType("proxy");

                    clientId = proxyResponse.getClientId();

                    accessLogger.info(
                            String.format("Registering new proxy connection[%s] for %s[%s]",
                                    tcpConnection.getConnectionId(),
                                    tcpConnection.getRemoteAddress(),
                                    clientId
                            )
                    );

                    controlConnection = controlConnectionRegistry.get(clientId);

                    if (controlConnection == null){

                        //  can not find the client id, the control connection
                        //has been closed or it is a illegal
                        tcpConnection.close();
                        accessLogger.error(
                                String.format("No client found for identifier:%s",
                                        proxyResponse.getClientId()
                                )
                        );

                        break;
                    }

                    controlConnection.putProxy(tcpConnection);

                    break;

                }

                default:
                    runtimeLogger.error("Unknown message");
            }


    }
}
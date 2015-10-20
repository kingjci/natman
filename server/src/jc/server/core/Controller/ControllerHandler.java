package jc.server.core.Controller;

import jc.Random;
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
import java.util.HashSet;
import java.util.Set;

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
    private final Set<String> userLoginIn;

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
        this.userLoginIn = new HashSet<>();
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
                try{
                    tcpConnection.close();
                }catch (IOException ee){
                    runtimeLogger.error(
                            String.format(
                                    "Fail to close connection[%s] from %s",
                                    tcpConnection.getConnectionId(),
                                    tcpConnection.getRemoteAddress()
                            )
                    );
                    runtimeLogger.error(ee.getMessage(), e);
                }

                return;
            }

            ControlConnection controlConnection;

            switch (message.getMessageType()) {

                case "AuthRequest":

                    AuthRequest authRequest = (AuthRequest) message;
                    AuthResponse authResponse = new AuthResponse(option.getVersion());
                    //here is the auth process




                    //Username and password is correct, check whether the user has already login in
                    if (userLoginIn.contains(authRequest.getUsername())){

                        String error = String.format(
                                "Fail to auth, user %s has already login in",
                                authRequest.getUsername()
                        );

                        authResponse.refuse(error);
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

                    if (authRequest.getVersion() < option.getMinVersion()){

                        String error = String.format(
                                "Incompatible versions. Server current/min version is %f/%f, client %s. Download a new version",
                                option.getVersion(),
                                option.getMinVersion(),
                                authRequest.getVersion()
                        );

                        authResponse.refuse(error);
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
                        if (authRequest.getUsername() == null | "".equalsIgnoreCase(authRequest.getUsername())){
                            clientId = random.getRandomString(16);
                        }else {
                            clientId = authRequest.getUsername();
                            userLoginIn.add(authRequest.getUsername());
                        }
                    }else {
                        clientId = authRequest.getClientId();
                    }
                    authResponse.setClientId(clientId);

                    accessLogger.info(
                            String.format("Auth client %s[%s] successfully",
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
                    controlConnection.start();

                    break;

                case "ProxyResponse":{

                    ProxyResponse proxyResponse = (ProxyResponse) message;

                    tcpConnection.setType("proxy");

                    clientId = proxyResponse.getClientId();

                    accessLogger.debug(
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

                        accessLogger.error(
                                String.format("No client found for identifier:%s",
                                        proxyResponse.getClientId()
                                )
                        );

                        try {
                            tcpConnection.close();
                        }catch (IOException e){
                            runtimeLogger.error(
                                    String.format(
                                            "Fail to close control connection[%s]",
                                            tcpConnection.getConnectionId()
                                    )
                            );
                        }

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
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
import jc.server.core.PublicTunnel.TCP.PublicTCPTunnelRegistry;
import jc.server.core.Users.Users;
import org.apache.log4j.Logger;

import java.io.IOException;

public class ControllerHandler extends Thread {

    private final TCPConnection tcpConnection;
    private final ControlConnectionRegistry controlConnectionRegistry;
    private final PublicTCPTunnelRegistry publicTCPTunnelRegistry;
    private final Random random;
    private final Config config;
    private final Option option;
    private final Logger runtimeLogger;
    private final Logger accessLogger;

    public ControllerHandler(
            TCPConnection tcpConnection,
            Controller controller
    ) {
        this.tcpConnection = tcpConnection;
        this.controlConnectionRegistry = controller.getControlConnectionRegistry();
        this.publicTCPTunnelRegistry = controller.getPublicTCPTunnelRegistry();
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
    public PublicTCPTunnelRegistry getPublicTCPTunnelRegistry() {
        return publicTCPTunnelRegistry;
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

    @Override
    public void run() {

            Message message;
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

                    if (config.isAuth()){

                        String username = authRequest.getUsername();
                        String password = authRequest.getPassword();

                        if (username == null | "".equals(username) | password == null | "".equals(password)){
                            String error = "Absent of username or password";

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
                            accessLogger.info(
                                    String.format(
                                            "Fail to auth: Absent of username or password from %s[%s]",
                                            tcpConnection.getRemoteAddress(),
                                            tcpConnection.getConnectionId()
                                    )
                            );
                            return;
                        }

                        Users users = config.getUsers();
                        boolean authResult = users.auth(username, password);

                        if (!authResult){

                            String error = "Incorrect username or password";

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
                            accessLogger.info(
                                    String.format(
                                            "Fail to auth: Incorrect username/password %s/%s from %s[%s]",
                                            username,
                                            password,
                                            tcpConnection.getRemoteAddress(),
                                            tcpConnection.getConnectionId()
                                    )
                            );
                            return;
                        }

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
                        accessLogger.info(
                                String.format(
                                        "Fail to auth: Incorrect Incompatible versions %f from %s[%s]",
                                        authRequest.getVersion(),
                                        tcpConnection.getRemoteAddress(),
                                        tcpConnection.getConnectionId()
                                )
                        );
                        return;
                    }

                    String clientId = authRequest.isNew()? random.getRandomClientId():authRequest.getClientId();

                    authResponse.setClientId(clientId);

                    accessLogger.info(
                            String.format("Success to auth %s[%s][%s]",
                                    authRequest.getUsername(),
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

                    controlConnection = new ControlConnection(this, clientId);
                    //If the client already has a control connection, old one will be closed
                    controlConnectionRegistry.register(controlConnection);
                    controlConnection.start();

                    break;

                case "ProxyResponse":{

                    ProxyResponse proxyResponse = (ProxyResponse) message;
                    tcpConnection.setType("proxy");
                    accessLogger.debug(
                            String.format("Registering new proxy connection[%s] for %s[%s]",
                                    tcpConnection.getConnectionId(),
                                    tcpConnection.getRemoteAddress(),
                                    proxyResponse.getClientId()
                            )
                    );

                    controlConnection = controlConnectionRegistry.get(proxyResponse.getClientId());

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
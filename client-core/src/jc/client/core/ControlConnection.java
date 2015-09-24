package jc.client.core;

import jc.message.AuthRequest;
import jc.message.AuthResponse;
import jc.message.Message;

import java.util.HashMap;
import java.util.Map;
import static jc.client.core.Utils.Dial;

/**
 * Created by ½ð³É on 2015/9/23.
 */
public class ControlConnection implements Runnable {

    private String id;
    private Map<String, PrivateTunnel> privateTunnels;
    private String serverAddr;
    private float serverVersion;
    private Controller controller;
    private String proxyUrl;
    private String authToken;
    private Map<String, PrivateTunnel> tunnels;
    private Map<String, TunnelConfiguration> requestIdToTunnelConfig;

    public ControlConnection(Controller controller, Config config){
        this.serverAddr = config.getServerAddr();
        this.proxyUrl = config.getHttpProxy();
        this.authToken = config.getAuthToken();
        this.tunnels  = new HashMap<String, PrivateTunnel>();
        this.controller = controller;
        this.requestIdToTunnelConfig = new HashMap<String, TunnelConfiguration>();
    }

    @Override
    public void run() {

        int maxWait = 30*1000;
        int wait = 1*1000;

        while (true){

            control();



        }

    }

    public void control(){
        Connection connection = null;
        if ("".equalsIgnoreCase(proxyUrl)){
            connection = Dial(serverAddr, 12345, "control" );
        }

        AuthRequest authRequest = new AuthRequest(id, 1.0f, 1.0f);

        connection.writeMessage(authRequest);

        AuthResponse authResponse =(AuthResponse) connection.readMessage();

        this.id = authResponse.getClientId();
        this.serverVersion = authResponse.getVersion();
        System.out.printf("Authenticated with server, client id: %s\n", this.id);















        if (connection != null){
            connection.close();
        }
    }
}

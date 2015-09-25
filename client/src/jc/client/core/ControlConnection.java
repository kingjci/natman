package jc.client.core;

import jc.Connection;
import jc.message.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static jc.Utils.Dial;
import static jc.client.core.Main.random;

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
    private Map<String, TunnelConfiguration> tunnelConfiguration;
    private Map<String, TunnelConfiguration> requestIdToTunnelConfig;
    private Long lastPingResponse;


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
        AuthResponse authResponse = null;

        try{
            connection.writeMessage(authRequest);

            authResponse =(AuthResponse) connection.readMessage();
        }catch (IOException e){
            e.printStackTrace();
        }


        this.id = authResponse.getClientId();
        this.serverVersion = authResponse.getVersion();
        System.out.printf("Authenticated with server, client id: %s\n", this.id);


        for (Map.Entry<String, TunnelConfiguration> entry : tunnelConfiguration.entrySet()){

            TunnelRequest tunnelRequest =
                    new TunnelRequest(random.getRandomString(8), "tcp", entry.getValue().getRemotePort());

            try {
                connection.writeMessage(tunnelRequest);
            }catch (IOException e){
                e.printStackTrace();
            }


            requestIdToTunnelConfig.put(tunnelRequest.getRequestId(), entry.getValue());

        }

        this.lastPingResponse = System.currentTimeMillis();
















        if (connection != null){
            connection.close();
        }
    }
}

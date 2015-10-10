package jc.client.core;

import jc.TCPConnection;
import jc.message.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static jc.client.core.Main.random;
import static jc.client.core.Utils.*;

/**
 * Created by 金成 on 2015/9/23.
 */
public class ControlConnection implements Runnable {

    private String clientId;
    private String serverAddr;
    private float serverVersion;
    private Controller controller;
    private String proxyUrl;
    private String authToken;
    private Map<String, PrivateTunnel> tunnels;
    private Map<String, TunnelConfiguration> tunnelConfiguration;//这个应该在LoadConfiguration中初始化
    private Map<String, TunnelConfiguration> requestIdToTunnelConfig;
    private Time lastPingResponse;

    public String getClientId() {
        return clientId;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public ControlConnection(Controller controller, Config config){

        this.serverAddr = config.getServerAddr();
        this.proxyUrl = config.getHttpProxy();
        this.authToken = config.getAuthToken();
        this.tunnels  = new HashMap<String, PrivateTunnel>();
        this.controller = controller;
        this.requestIdToTunnelConfig = new HashMap<String, TunnelConfiguration>();
        this.tunnelConfiguration = config.getTunnels();
        this.lastPingResponse = new Time();
    }

    @Override
    public void run() {

        int maxWait = 30*1000;
        int wait = 1*1000;

        while (true){

            control();

            try{
                Thread.sleep(wait);
            }catch (InterruptedException e){
                e.printStackTrace();
            }



        }

    }

    public void control(){

        TCPConnection TCPConnection = Dial(serverAddr, 12345, "control" );
        AuthRequest authRequest = new AuthRequest(clientId, 1.0f, 1.0f);
        AuthResponse authResponse = null;

        try{
            TCPConnection.writeMessage(authRequest);

            authResponse =(AuthResponse) TCPConnection.readMessage();
        }catch (IOException e){
            e.printStackTrace();
        }


        if (authResponse == null){
            return;
        }
        this.clientId = authResponse.getClientId();
        this.serverVersion = authResponse.getVersion();
        System.out.printf("[%s][ControlConnection]Authenticated with server, client id: %s\n", timeStamp(),this.clientId);


        for (Map.Entry<String, TunnelConfiguration> entry : tunnelConfiguration.entrySet()){

            TunnelConfiguration tunnelConfiguration = entry.getValue();
            TunnelRequest tunnelRequest =
                    new TunnelRequest(random.getRandomString(8), "tcp", tunnelConfiguration.getRemotePort(), tunnelConfiguration.getLocalPort());

            try {
                TCPConnection.writeMessage(tunnelRequest);
            }catch (IOException e){
                e.printStackTrace();
            }


            requestIdToTunnelConfig.put(tunnelRequest.getRequestId(), entry.getValue());

        }

        this.lastPingResponse.setTime(System.currentTimeMillis());

        Go(new HeartBeat(lastPingResponse, TCPConnection, this));

        while (true){

            Message message = null;

            try{
                message = TCPConnection.readMessage();
            }catch (IOException e){
                e.printStackTrace();
                System.out.printf("[%s]control connection is closed,prepare to exit\n", timeStamp());
                return;
                //退出清理
            }

            if (message == null){
                System.out.printf("[%s]receive null message\n", timeStamp());
                continue;
            }

            switch (message.getMessageType()){

                case "ProxyRequest":

                    Go(new Proxy(this));
                    break;

                case "PingResponse":

                    lastPingResponse.setTime(System.currentTimeMillis());
                    break;

                case "TunnelResponse":

                    TunnelResponse tunnelResponse = (TunnelResponse) message;
                    if (tunnelResponse.hasError()){
                        String error = String.format("[%s]Server failed to allocate tunnel: %s\n", timeStamp(),tunnelResponse.getError());
                        System.out.printf(error);
                        shutDown(error);
                        //准备退出程序
                        return;
                    }

                    PrivateTunnel privateTunnel =
                            new PrivateTunnel(tunnelResponse.getUrl(), "127.0.0.1", tunnelResponse.getLocalPort(),tunnelResponse.getProtocol());

                    tunnels.put(tunnelResponse.getUrl(), privateTunnel);
                    System.out.printf("[%s][ControlConnection]PrivateTunnel established at %s\n", timeStamp(),tunnelResponse.getUrl());

                    break;

                default:

                    System.out.printf("[%s]Ignoring unknown control message\n", timeStamp());

            }
        }

    }

    public PrivateTunnel getPrivateTunnel(String privateTunnel){
        return tunnels.get(privateTunnel);
    }

    public void shutDown(String reason){

    }
}

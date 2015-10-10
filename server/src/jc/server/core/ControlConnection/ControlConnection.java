package jc.server.core.ControlConnection;


import jc.Random;
import jc.message.*;
import jc.server.core.PublicTunnel.PublicTunnel;
import jc.Version;
import jc.TCPConnection;
import jc.Time;
import jc.server.core.PublicTunnel.PublicTunnelRegistry;

import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static jc.Utils.Go;
import static jc.Utils.timeStamp;

public class ControlConnection implements Runnable{

    private final AuthRequest authRequest;
    private final TCPConnection tcpConnection;
    private final Time lastPing;
    private final BlockingQueue<TCPConnection> proxies = new LinkedBlockingQueue<TCPConnection>();
    private final String clientId;
    private final Timer pingChecker;
    private final String ip;
    private final Random random;
    private final PublicTunnelRegistry publicTunnelRegistry;

    public String getClientId() {
        return clientId;
    }

    public ControlConnection(
            TCPConnection tcpConnection,
            PublicTunnelRegistry publicTunnelRegistry,
            Random random,
            AuthRequest authRequest){
        this.tcpConnection = tcpConnection;
        this.publicTunnelRegistry = publicTunnelRegistry;
        this.random = random;
        this.authRequest = authRequest;
        this.ip = tcpConnection.getRemoteAddr();
        this.tcpConnection.setType("control");

        this.pingChecker = new Timer();
        this.lastPing = new Time(System.currentTimeMillis());


        if (authRequest.isNew()){
            this.clientId = random.getRandomString(16);
        }else {
            this.clientId = authRequest.getClientId();
        }

        //判断客户端版本与服务器版本是否兼容
        if (authRequest.getVersion() < 0){
            System.out.println("Incompatible versions. Server %s, client %s. Download a new version");
            AuthResponse authResponse = new AuthResponse("Incompatible versions");
            try {
                tcpConnection.writeMessage(authResponse);
            }catch (IOException e){
                e.printStackTrace();
                //关闭这个control connection
            }

        }


        AuthResponse authResponse = new AuthResponse(Version.Current, this.clientId);
        if (!authRequest.isNew()){
            authResponse.setReconnect(true);
        }

        try{
            this.tcpConnection.writeMessage(authResponse);
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public TCPConnection getProxy(){

        TCPConnection proxyTCPConnection = null;
        if (proxies.size() == 0){
            //System.out.printf("[%s][ControlConnection]No proxy in pool, requesting proxy from %s[%s]\n",
                    //timeStamp(),this.ip, this.clientId);
            ProxyRequest proxyRequest = new ProxyRequest();
            try{
                tcpConnection.writeMessage(proxyRequest);
            }catch (IOException e){
                e.printStackTrace();
            }

        }

        try{
            proxyTCPConnection = proxies.take();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }



        if (proxyTCPConnection == null){
            System.out.printf("[%s][ControlConnection]No proxy connections available\n", timeStamp());
        }

        return proxyTCPConnection;

    }

    public void registerProxy(TCPConnection tcpConnection){

        try{
            proxies.put(tcpConnection);
        }catch (InterruptedException e){
            e.printStackTrace();
        }


    }

    public String getIp() {
        return ip;
    }

    public void close(){
        tcpConnection.close();
    }

    public void shutDown(){
        //用来关闭这个control connection
    }



    @Override
    public void run() {


        this.pingChecker.schedule(new ControlConnectionHeartBeatChecker(tcpConnection, lastPing), 0, 10*1000);

        while (true){

            try{
                Message message = tcpConnection.readMessage();
                switch (message.getMessageType()){

                    case "PublicTunnelRequest":

                        PublicTunnelRequest publicTunnelRequest = (PublicTunnelRequest) message;
                        String url = String.format("tcp://%s:%d", "127.0.0.1", publicTunnelRequest.getRemotePort());
                        PublicTunnel publicTunnel = publicTunnelRegistry.get(url);
                        if (publicTunnel != null){

                            PublicTunnelResponse publicTunnelResponse = new PublicTunnelResponse(String.format("tunnel %d is already in use", publicTunnelRequest.getRemotePort()));
                            try{
                                tcpConnection.writeMessage(publicTunnelResponse);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                            continue;
                        }

                        publicTunnel = new PublicTunnel(publicTunnelRequest, this, random);
                        publicTunnelRegistry.register(clientId, url, publicTunnel);

                        Go(publicTunnel);
                        PublicTunnelResponse publicTunnelResponse =
                                new PublicTunnelResponse(publicTunnel.getUrl(),
                                        publicTunnelRequest.getProtocol(),
                                        publicTunnelRequest.getRequestId(),
                                        publicTunnelRequest.getLocalPort());
                        try{
                            this.tcpConnection.writeMessage(publicTunnelResponse);
                        }catch (IOException e){
                            e.printStackTrace();
                        }

                        break;

                    case "PingRequest":

                        this.lastPing.setTime(System.currentTimeMillis());
                        PingResponse pingResponse = new PingResponse();
                        try{
                            tcpConnection.writeMessage(pingResponse);
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        break;

                    default:

                        System.out.printf("[%s][ControlConnection]unknown message\n", timeStamp());
                        break;
                }

            }catch (IOException e){
                //e.printStackTrace();
                System.out.printf("[%s][ControlConnection]control connection to %s[%s] exit\n", timeStamp(), ip,clientId);
                //需要清理这个control connection对应的public tunnel以及相关的记录
                this.pingChecker.cancel();
                publicTunnelRegistry.delete(clientId);
                return;
            }

        }
    }
}
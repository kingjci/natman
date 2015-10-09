package jc.server.core;


import jc.message.*;
import jc.server.core.PublicTunnel.PublicTunnel;
import jc.Version;
import jc.Connection;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static jc.server.core.Main.random;
import static jc.server.core.Main.controlConnectionRegistry;
import static jc.server.core.Utils.timeStamp;

public class ControlConnection implements Runnable{

    private AuthRequest authRequest;

    private final Connection connection;

    protected Time lastPing = new Time();

    private List<PublicTunnel> publicTunnels = new LinkedList<PublicTunnel>();

    private BlockingQueue<Connection> proxies = new LinkedBlockingQueue<Connection>();

    private String clientId;

    private Timer pingChecker;

    private String ip;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Connection getConnection() {
        return connection;
    }

    public ControlConnection(Connection connection, AuthRequest authRequest){

        this.connection = connection;

        this.ip = authRequest.getIP();

        this.connection.setType("control");

        lastPing.setTime(System.currentTimeMillis());

        this.clientId = authRequest.getClientId();
        if (this.clientId == null | "".equalsIgnoreCase(this.clientId)){
            this.clientId = random.getRandomString(16);
        }


        if (authRequest.getVersion() < 0){
            System.out.println("Incompatible versions. Server %s, client %s. Download a new version");
            AuthResponse authResponse = new AuthResponse("Incompatible versions");
            try {
                connection.writeMessage(authResponse);
                connection.close();
            }catch (IOException e){
                e.printStackTrace();
            }


        }

        if (controlConnectionRegistry.has(authRequest.getClientId())){
            ControlConnection controlConnection = controlConnectionRegistry.get(authRequest.getClientId());
            controlConnection.close();
        }

        controlConnectionRegistry.register(this.clientId, this);


        AuthResponse authResponse = new AuthResponse(Version.Current, this.clientId);
        try{

            this.connection.writeMessage(authResponse);


        }catch (IOException e){
            e.printStackTrace();
        }

    }


    private class PingCheckerTimerTask extends TimerTask{

        private Time lastPing;

        public PingCheckerTimerTask(Time lastPing){
            this.lastPing = lastPing;
        }

        @Override
        public void run() {
            long currentTimeMillis = System.currentTimeMillis();
            long diff = currentTimeMillis - lastPing.getTime();
            if (diff > 30*1000){
                System.out.printf("[%s][ControlConnection]Lost heartbeat\n", timeStamp());
                connection.close();//强制关闭这个control connection里面的socket，这样
                //control connection 里面的主循环会被强制退出，退出线程
                //准备关闭控制连接
            }
        }
    }


    public void bindTunnel(TunnelRequest tunnelRequest){

        //controlConnection.bindTunnel->new Tunnel->tunnelRegistry.register
        //最终通过在controlConnection中调用bindTunnel实现新建一个tunnel并注册的功能

        //服务器监听了tunnelRequest中指定的端口
        PublicTunnel publicTunnel = new PublicTunnel(tunnelRequest, this);
        TunnelResponse tunnelResponse =
                new TunnelResponse(publicTunnel.getUrl(),
                        tunnelRequest.getProtocol(),
                        tunnelRequest.getRequestId(),
                        tunnelRequest.getLocalPort());
        try{

            this.connection.writeMessage(tunnelResponse);


        }catch (IOException e){
            e.printStackTrace();
        }

        publicTunnels.add(publicTunnel);
    }

    public Connection getProxy(){

        Connection proxyConnection = null;


            if (proxies.size() == 0){
                System.out.printf("[%s][ControlConnection]No proxy in pool, requesting proxy from %s[%s]\n",
                        timeStamp(),this.ip, this.clientId);
                ProxyRequest proxyRequest = new ProxyRequest();
                try{
                    connection.writeMessage(proxyRequest);
                }catch (IOException e){
                    e.printStackTrace();
                }

            }

            try{
                proxyConnection = proxies.take();
            }catch (InterruptedException e){
                e.printStackTrace();
            }



        if (proxyConnection == null){
            System.out.printf("[%s][ControlConnection]No proxy connections available\n", timeStamp());
        }

        return proxyConnection;

    }

    public void registerProxy(Connection connection){

        try{
            proxies.put(connection);
        }catch (InterruptedException e){
            e.printStackTrace();
        }


    }

    public String getIp() {

        return ip;
    }

    public void close(){

        connection.close();
    }



    @Override
    public void run() {

        this.pingChecker = new Timer();
        this.pingChecker.schedule(new PingCheckerTimerTask(lastPing), 0, 10*1000);

        while (true){


            try{

                Message message = connection.readMessage();

                switch (message.getMessageType()){

                    case "TunnelRequest":

                        TunnelRequest tunnelRequest = (TunnelRequest) message;
                        bindTunnel(tunnelRequest);
                        break;

                    case "PingRequest":

                        PingRequest pingRequest = (PingRequest) message;

                        this.lastPing.setTime(System.currentTimeMillis());
                        PingResponse pingResponse = new PingResponse();
                        try{
                            connection.writeMessage(pingResponse);
                        }catch (IOException e){
                            e.printStackTrace();
                        }

                        //System.out.printf("Ping from %s at %s\n", pingRequest.getClientId(), pingRequest.getPingTime());
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
                return;
            }

        }
    }
}
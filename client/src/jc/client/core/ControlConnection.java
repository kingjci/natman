package jc.client.core;

import jc.Random;
import jc.TCPConnection;
import jc.client.core.command.Command;
import jc.client.core.command.QuitCommand;
import jc.message.*;
import jc.Time;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static jc.Utils.*;

/**
 * Created by 金成 on 2015/9/23.
 */
public class ControlConnection implements Runnable {

    private String clientId;
    private String serverAddress;
    private float serverVersion; //当客户端版本过低时退出，会给出服务器的版本号
    private Map<String, PrivateTunnel> privateTunnels;
    //这个应该在LoadConfiguration中初始化，指向config中的端口配置
    private Map<String, PublicTunnelConfiguration> publicTunnelConfiguration;
    private Time lastPingResponse;
    private Random random;
    private TCPConnection tcpConnection;
    private BlockingQueue<Command> cmds;

    public String getClientId() {
        return clientId;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public ControlConnection(Config config, Random random, BlockingQueue<Command> cmds){

        this.serverAddress = config.getServerAddress();
        this.privateTunnels = new HashMap<>();
        this.publicTunnelConfiguration = config.getPublicTunnelConfiguration();
        this.lastPingResponse = new Time();
        this.random = random;
        this.cmds = cmds;
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

        TCPConnection tcpConnection =
                Dial(serverAddress, 12345, "control", random.getRandomString(8) );
        this.tcpConnection = tcpConnection;
        AuthRequest authRequest = new AuthRequest(clientId, 1.0f);
        AuthResponse authResponse = null;

        try{
            tcpConnection.writeMessage(authRequest);
            authResponse =(AuthResponse) tcpConnection.readMessage();
        }catch (IOException e){
            e.printStackTrace();
        }


        if (authResponse == null){
            return;
        }

        this.clientId = authResponse.getClientId();
        this.serverVersion = authResponse.getVersion();
        System.out.printf("[%s][ControlConnection]Authenticated with server, client id: %s\n", timeStamp(),this.clientId);


        //当客户端第一次连接到服务器的时候，发送端口绑定请求。如果客户端是reconnect为了
        //防止服务器同一个端口多次绑定出错，跳过发送端口绑定请求的过程
        if (!authResponse.isReconnect()){

            for (Map.Entry<String, PublicTunnelConfiguration> entry : publicTunnelConfiguration.entrySet()){

                PublicTunnelConfiguration publicTunnelConfiguration = entry.getValue();
                PublicTunnelRequest publicTunnelRequest =
                        new PublicTunnelRequest(random.getRandomString(8), "tcp", publicTunnelConfiguration.getRemotePort(), publicTunnelConfiguration.getLocalPort());

                try {
                    tcpConnection.writeMessage(publicTunnelRequest);
                }catch (IOException e){
                    e.printStackTrace();
                }

            }

        }


        this.lastPingResponse.setTime(System.currentTimeMillis());

        Go(new ControlConnectionHeartBeat(lastPingResponse, tcpConnection, this));

        while (true){

            Message message = null;

            try{
                message = tcpConnection.readMessage();
            }catch (IOException e){
                e.printStackTrace();
                System.out.printf("[%s]control connection is closed,prepare to exit\n", timeStamp());
                //当读取消息出现错误时候，退出control程序，休眠一段时间后，重新调用control进行连接
                return;
                //退出清理
            }

            switch (message.getMessageType()){

                case "ProxyRequest":

                    Go(new Proxy(this, random));
                    break;

                case "PingResponse":

                    lastPingResponse.setTime(System.currentTimeMillis());
                    break;

                case "PublicTunnelResponse":

                    PublicTunnelResponse publicTunnelResponse = (PublicTunnelResponse) message;
                    if (publicTunnelResponse.hasError()){
                        String error = String.format("[%s]Server failed to allocate tunnel: %s\n", timeStamp(), publicTunnelResponse.getError());
                        System.out.printf(error);
                        shutDown(error);
                        //准备退出程序
                        shutDown(publicTunnelResponse.getError());
                        return;
                    }

                    PrivateTunnel privateTunnel =
                            new PrivateTunnel(publicTunnelResponse.getUrl(), "127.0.0.1", publicTunnelResponse.getLocalPort(), publicTunnelResponse.getProtocol());

                    privateTunnels.put(publicTunnelResponse.getUrl(), privateTunnel);
                    System.out.printf("[%s][ControlConnection]PrivateTunnel established at %s\n", timeStamp(), publicTunnelResponse.getUrl());

                    break;

                default:

                    System.out.printf("[%s]Ignoring unknown control message\n", timeStamp());

            }
        }

    }

    public PrivateTunnel getPrivateTunnel(String privateTunnel){
        return privateTunnels.get(privateTunnel);
    }

    public void shutDown(String reason){
        tcpConnection.close();
        QuitCommand quitCommand = new QuitCommand(reason);
        try{
            cmds.put(quitCommand);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }
}

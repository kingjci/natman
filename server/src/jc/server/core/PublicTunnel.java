package jc.server.core;

import jc.message.ProxyStart;
import jc.message.TunnelRequest;
import jc.Connection;
import jc.server.core.control.ControlConnection;

import static jc.server.core.Main.*;
import static jc.Utils.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 金成 on 2015/9/8.
 */
public class PublicTunnel {

    protected TunnelRequest tunnelRequest;
    protected long start;
    protected String url;
    protected ServerSocket serverSocket;
    protected ControlConnection controlConnection;
    protected int closing;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public PublicTunnel(TunnelRequest tunnelRequest, ControlConnection controlConnection){

        this.tunnelRequest = tunnelRequest;
        this.start = System.currentTimeMillis();
        this.controlConnection = controlConnection;

        switch (tunnelRequest.getProtocol()){

            case "tcp":

                int port = tunnelRequest.getRemotePort();
                try{
                    this.serverSocket.bind(new InetSocketAddress("0.0.0.0", port));
                }catch (IOException e){
                    e.printStackTrace();
                }
                this.url = String.format("tcp://%s:%d", Main.options.getDomain(), tunnelRequest.getRemotePort());

                int result = tunnelRegistry.register(url, this);
                if (result != 0){
                    System.out.printf("TCP listener bound, but failed to register %s\n", url);
                }

                Go(new TunnelListenHandler());



                break;

            case "http":

                //暂时不使用这里
                break;

            default:
                System.out.printf("Protocal %s is not supported\n", tunnelRequest.getProtocol());
                return;


        }





    }

    private class TunnelListenHandler implements Runnable{

        @Override
        public void run() {
            while (true){
                try{
                    Socket socket = serverSocket.accept();
                    Connection connection = new Connection(socket, "public");
                    System.out.printf("New publicConnection from %s", socket.getInetAddress().getHostAddress());
                    Go(new PublicConnectionHandler(connection));
                }catch (IOException e){
                    e.printStackTrace();
                }



            }
        }
    }

    private class PublicConnectionHandler implements Runnable{

        private Connection publicConnection;

        public PublicConnectionHandler(Connection publicConnection){
            this.publicConnection = publicConnection;
        }

        @Override
        public void run() {



            long start = System.currentTimeMillis();
            //统计来自public connection相关信息开始


            Connection proxyConnection = controlConnection.getProxy();
            if (proxyConnection == null){
                System.out.println("Tunnel->PublicConnectionHandler");return;
            }

            ProxyStart proxyStart = new ProxyStart(url, publicConnection.getRemoteAddr());

            //会阻塞在这里一直传送信息
            Join(proxyConnection, publicConnection);

            publicConnection.close();
            proxyConnection.close();
        }
    }



}

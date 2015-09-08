package jc.server.core.connection;

import jc.message.ProxyStart;
import jc.message.TunnelRequest;
import jc.server.core.Main;
import static jc.server.core.Main.*;
import static jc.server.core.Utils.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 金成 on 2015/9/8.
 */
public class Tunnel {

    private TunnelRequest tunnelRequest;
    private long start;
    private String url;
    private ServerSocket serverSocket;
    private ControlConnection controlConnection;
    private int closing;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Tunnel(TunnelRequest tunnelRequest, ControlConnection controlConnection){

        this.tunnelRequest = tunnelRequest;
        this.start = System.currentTimeMillis();
        this.controlConnection = controlConnection;

        switch (tunnelRequest.getProtocol()){

            case "tcp":
                int port = tunnelRequest.getRemotePort();
                try{
                    this.serverSocket.bind(new InetSocketAddress("127.0.0.1", port));
                }catch (IOException e){
                    e.printStackTrace();
                }
                this.url = String.format("tcp://%s:%d", Main.options.getDomain(), tunnelRequest.getRemotePort());

                int result = tunnelRegistry.Register(url, this);
                if (result != 0){
                    System.out.printf("TCP listener bound, but failed to register %s\n", url);
                }

                go(new TunnelListenHandler());



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
                    go(new PublicConnectionHandler(connection));
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

            try{
                publicConnection.getSocket().close();
                long start = System.currentTimeMillis();
                //统计来自public connection相关信息开始


                Connection proxyConnection = null;

                for (int i = 0 ; i < 20 ; i++){

                    proxyConnection = controlConnection.GetProxy();
                    if (proxyConnection != null){
                        break;
                    }
                }
                System.out.printf("Got proxy connection %s\n", proxyConnection.toString());
                WriteMessage(proxyConnection, new ProxyStart(url, publicConnection.getSocket().getInetAddress().getHostAddress()));
                Join(publicConnection, proxyConnection);
                publicConnection.Close();





            }catch (IOException e){
                e.printStackTrace();
            }





        }
    }



}

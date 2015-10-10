package jc.server.core.PublicTunnel;

import jc.TCPConnection;
import jc.message.TunnelRequest;
import jc.server.core.Main;
import jc.server.core.ControlConnection;

import static jc.server.core.Main.*;
import static jc.server.core.Utils.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 金成 on 2015/9/8.
 */
public class PublicTunnel implements Runnable{

    protected TunnelRequest tunnelRequest;
    protected long start;
    protected int port;
    protected String url;
    protected String protocol;
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

                this.port = tunnelRequest.getRemotePort();
                this.protocol = "tcp";
                this.url = String.format("tcp://%s:%d", Main.options.getDomain(), tunnelRequest.getRemotePort());

                tunnelRegistry.register(url, this);

                try{
                    this.serverSocket = new ServerSocket();
                    this.serverSocket.bind(new InetSocketAddress("0.0.0.0", port));
                }catch (IOException e){
                    e.printStackTrace();
                }


                break;

            case "http":

                //暂时不使用这里
                break;

            case "udp":

                //暂时不使用这里
                break;

            default:

                System.out.printf("[%s][PublicTunnel]Protocol %s is not supported\n", timeStamp(),tunnelRequest.getProtocol());

        }
    }

    @Override
    public void run() {


        switch (protocol){

            case "tcp":

                while (true){
                    try{
                        Socket socket = serverSocket.accept();
                        TCPConnection TCPConnection = new TCPConnection(socket, "public", random.getRandomString(8));
                        System.out.printf("[%s][PublicTunnel]New public Connection[%s] from %s\n",
                                timeStamp(), TCPConnection.getConnectionId(),socket.getInetAddress().getHostAddress());
                        Go(new PublicTunnelTCPHandler(controlConnection, TCPConnection, url));
                    }catch (IOException e){
                        e.printStackTrace();

                    }
                }


            case "http":



                //暂时不使用这里
                break;


            case "udp":


                //暂时不使用这里
                break;

        }

    }
}

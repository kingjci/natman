package jc.server.core.PublicTunnel;

import jc.Random;
import jc.TCPConnection;
import jc.message.PublicTunnelRequest;
import jc.server.core.ControlConnection.ControlConnection;

import static jc.Utils.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 金成 on 2015/9/8.
 */
public class PublicTunnel implements Runnable{

    protected PublicTunnelRequest publicTunnelRequest;
    protected long start;
    protected int port;
    protected String url;
    protected String protocol;
    protected ServerSocket serverSocket;
    protected ControlConnection controlConnection;
    protected int closing;
    private Random random;

    public String getUrl() {
        return url;
    }

    public PublicTunnel(PublicTunnelRequest publicTunnelRequest, ControlConnection controlConnection, Random random){


        this.publicTunnelRequest = publicTunnelRequest;
        this.start = System.currentTimeMillis();
        this.controlConnection = controlConnection;
        this.random = random;

        switch (publicTunnelRequest.getProtocol()){

            case "tcp":

                this.port = publicTunnelRequest.getRemotePort();
                this.protocol = "tcp";
                //"127.0.0.1" 这个是服务器的域名，应该从配置文件或者命令行中获取
                this.url = String.format("tcp://%s:%d", "127.0.0.1", publicTunnelRequest.getRemotePort());


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

                System.out.printf("[%s][PublicTunnel]Protocol %s is not supported\n", timeStamp(), publicTunnelRequest.getProtocol());

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

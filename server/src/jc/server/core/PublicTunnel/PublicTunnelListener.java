package jc.server.core.PublicTunnel;

import jc.Connection;
import jc.server.core.ControlConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static jc.server.core.Main.random;
import static jc.server.core.Utils.Go;
import static jc.server.core.Utils.timeStamp;

/**
 * Created by ½ð³É on 2015/10/9.
 */
public class PublicTunnelListener implements Runnable{

    private ControlConnection controlConnection;
    private ServerSocket serverSocket;
    private String url;

    public PublicTunnelListener(ControlConnection controlConnection ,int port, String url){

        try{
            this.serverSocket = new ServerSocket();
            this.serverSocket.bind(new InetSocketAddress("0.0.0.0", port));
        }catch (IOException e){
            e.printStackTrace();
        }

        this.controlConnection = controlConnection;
        this.url = url;

    }



    @Override
    public void run() {
        while (true){
            try{
                Socket socket = serverSocket.accept();
                Connection connection = new Connection(socket, "public", random.getRandomString(8));
                System.out.printf("[%s][PublicTunnel]New public Connection[%s] from %s\n",
                        timeStamp(), connection.getConnectionId(),socket.getInetAddress().getHostAddress());
                Go(new PublicTunnelHandler(controlConnection,connection, url));
            }catch (IOException e){
                e.printStackTrace();

            }

        }
    }
}

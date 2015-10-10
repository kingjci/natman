package jc.server.core.ControlTunnel;

import jc.TCPConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static jc.server.core.Main.random;
import static jc.server.core.Utils.Go;
import static jc.server.core.Utils.timeStamp;

/**
 * Created by ��� on 2015/9/23.
 */
public class ControlTunnel implements Runnable{

    //ControlTunnel使用tcp协议，不再做区分
    private ServerSocket serverSocket;


    public ControlTunnel(int port){

        try{
            serverSocket = new ServerSocket(port);
            System.out.printf("[%s][ControlTunnel]Listening for control and proxy connections on %d\n",
                    timeStamp(),port);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        while (true){


            try{
                Socket socket = serverSocket.accept();
                //control tunnel收到一个tcp socket 连接，将其包装成TCPConnection
                TCPConnection tcpConnection = new TCPConnection(socket, "control/proxy", random.getRandomString(8));
                System.out.printf("[%s][ControlTunnelListener]New control/proxy connection[%s] from %s\n",
                        timeStamp(), tcpConnection.getConnectionId(), tcpConnection.getRemoteAddr());
                Go(new ControlTunnelHandler(tcpConnection));
            }catch (IOException e){
                e.printStackTrace();
            }
        }


    }
}

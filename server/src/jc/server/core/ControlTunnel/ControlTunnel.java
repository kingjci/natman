package jc.server.core.ControlTunnel;

import jc.Random;
import jc.TCPConnection;
import jc.server.core.ControlConnection.ControlConnectionRegistry;
import jc.server.core.PublicTunnel.PublicTunnel;
import jc.server.core.PublicTunnel.PublicTunnelRegistry;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static jc.Utils.Go;
import static jc.Utils.timeStamp;

/**
 * Created by ��� on 2015/9/23.
 */
public class ControlTunnel implements Runnable{

    private PublicTunnelRegistry publicTunnelRegistry;
    private ControlConnectionRegistry controlConnectionRegistry;
    private Random random;


    //ControlTunnel使用tcp协议，不再做区分
    private ServerSocket serverSocket;


    public ControlTunnel(
            int port,
            PublicTunnelRegistry publicTunnelRegistry,
            ControlConnectionRegistry controlConnectionRegistry,
            Random random
    ){

        //传递给ControlTunnelHandler用来注册创建control connection的时候使用
        this.publicTunnelRegistry = publicTunnelRegistry;
        this.controlConnectionRegistry = controlConnectionRegistry;
        this.random = random;

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

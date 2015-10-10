package jc.server.core.ControlTunnel;

import jc.message.AuthRequest;
import jc.message.Message;
import jc.TCPConnection;
import jc.message.ProxyResponse;
import jc.server.core.ControlConnection;

import java.io.IOException;

import static jc.server.core.Main.controlConnectionRegistry;
import static jc.server.core.Utils.Go;
import static jc.server.core.Utils.timeStamp;

/**
 * Created by 金成 on 2015/9/23.
 */
public class ControlTunnelHandler implements Runnable {

    protected TCPConnection tcpConnection;

    ControlTunnelHandler(TCPConnection tcpConnection) {
        this.tcpConnection = tcpConnection;
    }

    @Override
    public void run() {


            if (tcpConnection == null){
                System.out.printf("[%s][ControlTunnelHandler]connection is null\n", timeStamp());
                return;
            }

            //connection.getSocket().setSoTimeout(10 * 1000);
            Message message = null;
            try {
                message = tcpConnection.readMessage();
            }catch (IOException e){
                e.printStackTrace();
            }

            if (message == null){
                System.out.printf("[%s][ControlTunnelHandler]message is null\n", timeStamp());
                return;
            }

            ControlConnection controlConnection = null;

            switch (message.getMessageType()) {


                case "AuthRequest":

                    //control tunnel收到的这个TCPConnection发送了认证请求，表明该TCPConnection
                    //是一个controlConnection
                    controlConnection = new ControlConnection(tcpConnection, (AuthRequest)message);

                    Go(controlConnection);

                    break;

                case "ProxyResponse":{

                    //control tunnel收到的这个TCPConnection发送了ProxyResponse，
                    //表明该TCPConnection是一个proxy connection，放入到proxy队列中
                    ProxyResponse proxyResponse = (ProxyResponse) message;

                    tcpConnection.setType("proxy");

                    System.out.printf("[%s][ControlTunnelHandler]Registering new proxy connection[%s] for %s[%s]\n", timeStamp(), tcpConnection.getConnectionId(), proxyResponse.getIP(), proxyResponse.getClientId());

                    controlConnection = controlConnectionRegistry.get(proxyResponse.getClientId());

                    if (controlConnection == null){
                        //客户端向服务器发送了一个proxy连接，但是通过ProxyResponse中的clientId找不到该客户端的控制连接
                        //这是一种不正常的状态，一般不会出现。可能是服务器已经删除了该control connection但是客户端的control
                        //connection 还存在，这个时候直接关闭该proxy connection
                        tcpConnection.close();
                        System.out.printf("[%s][ControlTunnelHandler]No client found for identifier:%s\n" ,
                                timeStamp(),proxyResponse.getClientId());
                        break;
                    }

                    controlConnection.registerProxy(tcpConnection);

                    break;

                }

                default:
                    System.out.printf("[%s][ControlTunnelHandler]unknown message\n", timeStamp());

            }


    }
}
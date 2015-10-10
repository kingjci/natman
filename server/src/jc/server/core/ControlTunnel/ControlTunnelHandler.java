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
 * Created by ��� on 2015/9/23.
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

                    //control tunnel�յ������TCPConnection��������֤���󣬱�����TCPConnection
                    //��һ��controlConnection
                    controlConnection = new ControlConnection(tcpConnection, (AuthRequest)message);

                    Go(controlConnection);

                    break;

                case "ProxyResponse":{

                    //control tunnel�յ������TCPConnection������ProxyResponse��
                    //������TCPConnection��һ��proxy connection�����뵽proxy������
                    ProxyResponse proxyResponse = (ProxyResponse) message;

                    tcpConnection.setType("proxy");

                    System.out.printf("[%s][ControlTunnelHandler]Registering new proxy connection[%s] for %s[%s]\n", timeStamp(), tcpConnection.getConnectionId(), proxyResponse.getIP(), proxyResponse.getClientId());

                    controlConnection = controlConnectionRegistry.get(proxyResponse.getClientId());

                    if (controlConnection == null){
                        //�ͻ����������������һ��proxy���ӣ�����ͨ��ProxyResponse�е�clientId�Ҳ����ÿͻ��˵Ŀ�������
                        //����һ�ֲ�������״̬��һ�㲻����֡������Ƿ������Ѿ�ɾ���˸�control connection���ǿͻ��˵�control
                        //connection �����ڣ����ʱ��ֱ�ӹرո�proxy connection
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
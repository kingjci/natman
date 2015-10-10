package jc.server.core.Controller;

import jc.message.AuthRequest;
import jc.message.Message;
import jc.TCPConnection;
import jc.message.ProxyResponse;
import jc.server.core.ControlConnection.ControlConnection;

import java.io.IOException;

import static jc.server.core.Main.controlConnectionRegistry;
import static jc.Utils.Go;
import static jc.Utils.timeStamp;
import static jc.server.core.Main.publicTunnelRegistry;
import static jc.server.core.Main.random;

/**
 * Created by ��� on 2015/9/23.
 */
public class ControllerHandler implements Runnable {

    private TCPConnection tcpConnection;

    ControllerHandler(TCPConnection tcpConnection) {
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

                    AuthRequest authRequest = (AuthRequest) message;
                    //control tunnel�յ������TCPConnection��������֤���󣬱�����TCPConnection
                    //��һ��controlConnection
                    controlConnection = new ControlConnection(
                            tcpConnection,
                            publicTunnelRegistry,
                            random,
                            (AuthRequest)message);

                    if (controlConnectionRegistry.has(authRequest.getClientId())){
                        ControlConnection oldControlConnection = controlConnectionRegistry.get(authRequest.getClientId());
                        oldControlConnection.close();
                        //oldControlConnection.shutDown();
                    }

                    controlConnectionRegistry.register(controlConnection.getClientId(), controlConnection);

                    Go(controlConnection);

                    break;

                case "ProxyResponse":{

                    //control tunnel�յ������TCPConnection������ProxyResponse��
                    //������TCPConnection��һ��proxy connection�����뵽proxy������
                    ProxyResponse proxyResponse = (ProxyResponse) message;

                    tcpConnection.setType("proxy");

                    //System.out.printf("[%s][ControllerHandler]Registering new proxy connection[%s] for %s[%s]\n",
                            //timeStamp(),
                            //tcpConnection.getConnectionId(),
                            //tcpConnection.getRemoteAddr(),
                            //proxyResponse.getClientId());

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
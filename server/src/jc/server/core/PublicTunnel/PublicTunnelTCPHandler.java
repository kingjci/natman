package jc.server.core.PublicTunnel;

import jc.TCPConnection;
import jc.message.ProxyStart;
import jc.server.core.ControlConnection;

import java.io.IOException;

import static jc.server.core.Utils.Join;
import static jc.server.core.Utils.timeStamp;

/**
 * Created by ��� on 2015/10/9.
 */
public class PublicTunnelTCPHandler implements Runnable{

    private ControlConnection controlConnection;
    private TCPConnection publicTCPConnection;
    private String url;

    public PublicTunnelTCPHandler(ControlConnection controlConnection, TCPConnection publicTCPConnection, String url){
        this.controlConnection = controlConnection;
        this.publicTCPConnection = publicTCPConnection;
        this.url = url;
    }


    @Override
    public void run() {

        long start = System.currentTimeMillis();
        //ͳ������public connection�����Ϣ��ʼ

        TCPConnection proxyTCPConnection = null;

        //controlConnection.getProxy(); ���������������ͻ�������ProxyRequest�Ĺ���
        proxyTCPConnection = controlConnection.getProxy();

        if (proxyTCPConnection == null){
            //��ʱ��Ȼ�޷��ӿͻ��˻�ȡ��proxy connection������
            System.out.printf("[%s][PublicTunnelHandler]can not get proxy connection from %s[%s]\n", timeStamp(), controlConnection.getIp(),controlConnection.getClientId());
            return;
        }

        ProxyStart proxyStart = new ProxyStart(url, publicTCPConnection.getRemoteAddr());

        try {
            proxyTCPConnection.writeMessage(proxyStart);
        }catch (IOException e){
            e.printStackTrace();
        }

        //������������һֱ������Ϣ
        Join(proxyTCPConnection, publicTCPConnection);

    }
}
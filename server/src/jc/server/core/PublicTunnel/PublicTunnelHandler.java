package jc.server.core.PublicTunnel;

import jc.Connection;
import jc.message.ProxyStart;
import jc.server.core.ControlConnection;

import java.io.IOException;

import static jc.server.core.Utils.Join;
import static jc.server.core.Utils.timeStamp;

/**
 * Created by ��� on 2015/10/9.
 */
public class PublicTunnelHandler implements Runnable{

    private ControlConnection controlConnection;
    private Connection publicConnection;
    private String url;

    public PublicTunnelHandler(ControlConnection controlConnection, Connection publicConnection, String url){
        this.controlConnection = controlConnection;
        this.publicConnection = publicConnection;
        this.url = url;
    }


    @Override
    public void run() {



        long start = System.currentTimeMillis();
        //ͳ������public connection�����Ϣ��ʼ

        Connection proxyConnection = null;


        //controlConnection.getProxy(); ���������������ͻ�������ProxyRequest�Ĺ���
        proxyConnection = controlConnection.getProxy();


        if (proxyConnection == null){
            //��ʱ��Ȼ�޷��ӿͻ��˻�ȡ��proxy connection������
            System.out.printf("[%s][PublicTunnelHandler]can not get proxy connection from %s[%s]\n", timeStamp(), controlConnection.getIp(),controlConnection.getClientId());
            return;
        }

        ProxyStart proxyStart = new ProxyStart(url, publicConnection.getRemoteAddr());

        try {
            proxyConnection.writeMessage(proxyStart);
        }catch (IOException e){
            e.printStackTrace();
        }



        //������������һֱ������Ϣ
        Join(proxyConnection, publicConnection);

    }
}
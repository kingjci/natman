package jc.server.core.PublicTunnel;

import jc.Connection;
import jc.message.ProxyStart;
import jc.server.core.ControlConnection;

import java.io.IOException;

import static jc.server.core.Utils.Join;
import static jc.server.core.Utils.timeStamp;

/**
 * Created by 金成 on 2015/10/9.
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
        //统计来自public connection相关信息开始

        Connection proxyConnection = null;


        //controlConnection.getProxy(); 方法里面包含了向客户端申请ProxyRequest的过程
        proxyConnection = controlConnection.getProxy();


        if (proxyConnection == null){
            //此时任然无法从客户端获取到proxy connection，出错
            System.out.printf("[%s][PublicTunnelHandler]can not get proxy connection from %s[%s]\n", timeStamp(), controlConnection.getIp(),controlConnection.getClientId());
            return;
        }

        ProxyStart proxyStart = new ProxyStart(url, publicConnection.getRemoteAddr());

        try {
            proxyConnection.writeMessage(proxyStart);
        }catch (IOException e){
            e.printStackTrace();
        }



        //会阻塞在这里一直传送信息
        Join(proxyConnection, publicConnection);

    }
}
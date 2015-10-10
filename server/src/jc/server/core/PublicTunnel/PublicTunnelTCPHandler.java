package jc.server.core.PublicTunnel;

import jc.TCPConnection;
import jc.message.ProxyStart;
import jc.server.core.ControlConnection.ControlConnection;

import java.io.IOException;

import static jc.Utils.Join;
import static jc.Utils.timeStamp;

/**
 * Created by 金成 on 2015/10/9.
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

        //controlConnection.getProxy(); 方法里面包含了向客户端申请ProxyRequest的过程
        TCPConnection proxyTCPConnection = controlConnection.getProxy();

        if (proxyTCPConnection == null){
            //此时任然无法从客户端获取到proxy connection，出错。关闭这个public connection
            System.out.printf("[%s][PublicTunnelHandler]can not get proxy connection from %s[%s]\n", timeStamp(), controlConnection.getIp(),controlConnection.getClientId());
            return;
        }

        ProxyStart proxyStart = new ProxyStart(url, publicTCPConnection.getRemoteAddr());

        try {
            proxyTCPConnection.writeMessage(proxyStart);
        }catch (IOException e){
            e.printStackTrace();
        }

        //会阻塞在这里一直传送信息
        Join(proxyTCPConnection, publicTCPConnection);

    }
}
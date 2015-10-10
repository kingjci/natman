package jc.client.core;

import jc.TCPConnection;
import jc.message.ProxyResponse;
import jc.message.ProxyStart;

import java.io.IOException;

import static jc.client.core.Utils.Dial;
import static jc.client.core.Utils.Join;
import static jc.client.core.Utils.timeStamp;

/**
 * Created by 金成 on 2015/10/8.
 */
public class Proxy implements Runnable {

    private ControlConnection controlConnection;

    public Proxy(ControlConnection controlConnection){
        this.controlConnection = controlConnection;
    }


    @Override
    public void run() {

        TCPConnection proxyTCPConnection = Dial(controlConnection.getServerAddr(),"proxy");
        TCPConnection localTCPConnection = null;


        ProxyResponse proxyResponse = new ProxyResponse(controlConnection.getClientId());

        try{
            proxyTCPConnection.writeMessage(proxyResponse);
        }catch (IOException e){
            System.out.printf("[%s][Proxy]Failed to write ProxyResponse\n", timeStamp());
            e.printStackTrace();
            return;
        }

        ProxyStart proxyStart = null;
        try{
            proxyStart =(ProxyStart) proxyTCPConnection.readMessage();
            System.out.printf("[%s][Proxy]New proxy connection[%s] from %s\n",
                    timeStamp(), proxyTCPConnection.getConnectionId(), proxyStart.getPublicConnectionAddress());
        }catch (IOException e){
            System.out.printf("[%s][Proxy]Proxy connection[%s] is shutdown by server\n",
                    timeStamp(), proxyTCPConnection.getConnectionId());
            //代理连接被服务器关闭
            proxyTCPConnection.close();
            e.printStackTrace();
            return;
        }

        PrivateTunnel privateTunnel = controlConnection.getPrivateTunnel(proxyStart.getUrl());
        if (privateTunnel == null){
            System.out.printf("[%s][Proxy run]Couldn't find tunnel for proxy: %s\n", timeStamp(),proxyStart.getUrl());
            return;
        }

        localTCPConnection = Dial(privateTunnel.getLocalAddress(), privateTunnel.getLocalPort(), "private");
        if (localTCPConnection == null){
            System.out.printf("[%s][Proxy run]Failed to open private connection %s: %s\n", timeStamp(),privateTunnel.getLocalAddress() ,privateTunnel.getLocalPort());
            return;
        }

        Join(localTCPConnection, proxyTCPConnection);

    }
}

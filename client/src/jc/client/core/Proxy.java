package jc.client.core;

import jc.Connection;
import jc.message.ProxyResponse;
import jc.message.ProxyStart;

import java.io.IOException;

import static jc.client.core.Utils.Dial;
import static jc.client.core.Utils.Go;
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

        Connection proxyConnection = Dial(controlConnection.getServerAddr(),"proxy");
        Connection localConnection = null;


        ProxyResponse proxyResponse = new ProxyResponse(controlConnection.getClientId());

        try{
            proxyConnection.writeMessage(proxyResponse);
        }catch (IOException e){
            System.out.printf("[%s][Proxy]Failed to write ProxyResponse\n", timeStamp());
            e.printStackTrace();
            return;
        }

        ProxyStart proxyStart = null;
        try{
            proxyStart =(ProxyStart) proxyConnection.readMessage();
        }catch (IOException e){
            System.out.printf("[%s][Proxy]Proxy connection[%s] is shutdown by server\n",
                    timeStamp(), proxyConnection.getConnectionId());
            //代理连接被服务器关闭
            proxyConnection.close();
            //e.printStackTrace();
            return;
        }

        PrivateTunnel privateTunnel = controlConnection.getPrivateTunnel(proxyStart.getUrl());
        if (privateTunnel == null){
            System.out.printf("[%s][Proxy run]Couldn't find tunnel for proxy: %s\n", timeStamp(),proxyStart.getUrl());
            return;
        }

        localConnection = Dial(privateTunnel.getLocalAddress(), privateTunnel.getLocalPort(), "private");
        if (localConnection == null){
            System.out.printf("[%s][Proxy run]Failed to open private connection %s: %s\n", timeStamp(),privateTunnel.getLocalAddress() ,privateTunnel.getLocalPort());
            return;
        }

        Join(localConnection, proxyConnection);


    }
}

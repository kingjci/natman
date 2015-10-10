package jc.client.core;

import jc.Random;
import jc.TCPConnection;
import jc.message.ProxyResponse;
import jc.message.ProxyStart;

import java.io.IOException;

import static jc.Utils.Dial;
import static jc.Utils.Join;
import static jc.Utils.timeStamp;

/**
 * Created by 金成 on 2015/10/8.
 */
public class Proxy implements Runnable {

    private ControlConnection controlConnection;
    private Random random;

    public Proxy(ControlConnection controlConnection, Random random){
        this.controlConnection = controlConnection;
        this.random = random;
    }


    @Override
    public void run() {

        TCPConnection proxyTCPConnection =
                Dial(controlConnection.getServerAddress(),12345,"proxy", random.getRandomString(8));
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
            //System.out.printf("[%s][Proxy]New proxy connection[%s] from %s\n",
                    //timeStamp(), proxyTCPConnection.getConnectionId(), proxyStart.getPublicConnectionAddress());
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

        localTCPConnection =
                Dial(privateTunnel.getLocalAddress(), privateTunnel.getLocalPort(), "private", random.getRandomString(8));
        if (localTCPConnection == null){
            System.out.printf("[%s][Proxy run]Failed to open private connection %s: %s\n", timeStamp(),privateTunnel.getLocalAddress() ,privateTunnel.getLocalPort());
            return;
        }

        Join(localTCPConnection, proxyTCPConnection);

    }
}

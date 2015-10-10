package jc.client.core;

import jc.TCPConnection;

import java.io.IOException;

import static jc.client.core.Utils.Dial;
import static jc.client.core.Utils.timeStamp;

/**
 * Created by ½ð³É on 2015/10/9.
 */
public class PlayRequest implements Runnable{

    private PrivateTunnel tunnel;

    private byte[] payload;

    public PlayRequest(PrivateTunnel tunnel, byte[] payload){

        this.tunnel = tunnel;
        this.payload = payload;

    }

    @Override
    public void run() {
        TCPConnection localTCPConnection =
                Dial(this.tunnel.getLocalAddress(), this.tunnel.getLocalPort(), "private connection");

        if (localTCPConnection == null){
            System.out.printf("[%s][Controller]Failed to open private leg to %s: %d\n", timeStamp(),this.tunnel.getLocalAddress(), this.tunnel.getLocalPort());
            return;
        }

        try{
            localTCPConnection.write(payload);
            localTCPConnection.readAll();//???????????
        }catch (IOException e){
            e.printStackTrace();
        }



    }
}
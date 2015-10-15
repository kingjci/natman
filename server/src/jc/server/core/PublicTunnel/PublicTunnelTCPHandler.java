package jc.server.core.PublicTunnel;

import jc.TCPConnection;
import jc.message.ProxyStart;
import jc.server.core.ControlConnection.ControlConnection;

import java.io.IOException;

import static jc.Utils.Join;

public class PublicTunnelTCPHandler implements Runnable{

    private ControlConnection controlConnection;
    private TCPConnection publicTCPConnection;
    private String publicUrl;

    public PublicTunnelTCPHandler(ControlConnection controlConnection, TCPConnection publicTCPConnection, String publicUrl){
        this.controlConnection = controlConnection;
        this.publicTCPConnection = publicTCPConnection;
        this.publicUrl = publicUrl;
    }


    @Override
    public void run() {

        TCPConnection proxyTCPConnection = controlConnection.getProxy();

        ProxyStart proxyStart = new ProxyStart(publicUrl, publicTCPConnection.getRemoteAddress());
        try {
            proxyTCPConnection.writeMessage(proxyStart);
        }catch (IOException e){
            e.printStackTrace();
        }

        //join proxy tcp connection with public tcp connection
        Join(proxyTCPConnection, publicTCPConnection);

    }
}
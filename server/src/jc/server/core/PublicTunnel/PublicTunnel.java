package jc.server.core.PublicTunnel;

import jc.message.TunnelRequest;
import jc.server.core.Main;
import jc.server.core.ControlConnection;

import static jc.server.core.Main.*;
import static jc.server.core.Utils.*;

import java.net.ServerSocket;

/**
 * Created by 金成 on 2015/9/8.
 */
public class PublicTunnel {

    protected TunnelRequest tunnelRequest;
    protected long start;
    protected String url;
    protected ServerSocket serverSocket;
    protected ControlConnection controlConnection;
    protected int closing;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public PublicTunnel(TunnelRequest tunnelRequest, ControlConnection controlConnection){


        this.tunnelRequest = tunnelRequest;
        this.start = System.currentTimeMillis();
        this.controlConnection = controlConnection;

        switch (tunnelRequest.getProtocol()){

            case "tcp":

                int port = tunnelRequest.getRemotePort();

                this.url = String.format("tcp://%s:%d", Main.options.getDomain(), tunnelRequest.getRemotePort());

                tunnelRegistry.register(url, this);

                Go(new PublicTunnelListener(controlConnection,port, url));

                break;

            case "http":

                //暂时不使用这里
                break;

            default:
                System.out.printf("[%s][PublicTunnel]Protocal %s is not supported\n", timeStamp(),tunnelRequest.getProtocol());
                return;


        }





    }
}

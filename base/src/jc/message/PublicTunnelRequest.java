package jc.message;

import java.io.Serializable;

/**
 * Created by ��� on 2015/9/8.
 */
public class PublicTunnelRequest implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    private String clientId;
    private String protocol;
    private String subdomain;
    private int remotePort;
    private int localPort;

    public PublicTunnelRequest(String clientId, String protocol, String subdomain,int remotePort, int localPort){
        this.clientId = clientId;
        this.protocol = protocol;
        this.subdomain = subdomain;
        this.remotePort = remotePort;
        this.localPort = localPort;
    }

    public int getLocalPort() {
        return localPort;
    }

    public String getClientId() {
        return clientId;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getRemotePort() {
        return remotePort;
    }

    @Override
    public String getMessageType() {
        return "PublicTunnelRequest";
    }
}

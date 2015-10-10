package jc.message;

import java.io.Serializable;

/**
 * Created by 金成 on 2015/9/8.
 */
public class PublicTunnelRequest implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    private String RequestId;
    private String Protocol; // 目前只有tcp
    private int RemotePort;
    private int LocalPort;

    public PublicTunnelRequest(String requestId, String protocol, int remotePort, int localPort){
        this.RequestId = requestId;
        this.Protocol = protocol;
        this.RemotePort = remotePort;
        this.LocalPort = localPort;
    }

    public int getLocalPort() {
        return LocalPort;
    }

    public String getRequestId() {
        return RequestId;
    }

    public String getProtocol() {
        return Protocol;
    }

    public int getRemotePort() {
        return RemotePort;
    }

    @Override
    public String getMessageType() {
        return "PublicTunnelRequest";
    }
}

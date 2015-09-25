package jc.message;

import java.io.Serializable;

/**
 * Created by 金成 on 2015/9/8.
 */
public class TunnelRequest implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    private String RequestId;
    private String Protocol; // 目前只有tcp
    private int RemotePort;


    public TunnelRequest(String requestId, String protocol, int remotePort){
        this.RequestId = requestId;
        this.Protocol = protocol;
        this.RemotePort = remotePort;
    }

    public String getRequestId() {
        return RequestId;
    }

    public void setRequestId(String requestId) {
        RequestId = requestId;
    }

    public String getProtocol() {
        return Protocol;
    }

    public void setProtocol(String protocol) {
        Protocol = protocol;
    }

    public int getRemotePort() {
        return RemotePort;
    }

    public void setRemotePort(int remotePort) {
        RemotePort = remotePort;
    }

    @Override
    public String getMessageType() {
        return "TunnelRequest";
    }
}

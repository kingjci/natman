package jc.message;

import java.io.Serializable;

/**
 * Created by ½ð³É on 2015/9/8.
 */
public class PingRequest implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    private String clientId;

    private String ip;

    private long pingTime;

    public PingRequest(String clientId, long pingTime){
        this.clientId = clientId;
        this.pingTime = pingTime;
    }

    public long getPingTime() {
        return pingTime;
    }

    public void setPingTime(long pingTime) {
        this.pingTime = pingTime;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getMessageType() {
        return "PingRequest";
    }

    @Override
    public void setIP(String ip) {
        this.ip = ip;
    }

    @Override
    public String getIP() {
        return ip;
    }
}

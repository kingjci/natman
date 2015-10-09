package jc.message;

import java.io.Serializable;

/**
 * Created by ½ð³É on 2015/9/8.
 */
public class ProxyResponse implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    private String clientId;

    private String ip;

    public ProxyResponse(String clientId){
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getMessageType() {
        return "ProxyResponse";
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

package jc.message;

import java.io.Serializable;

/**
 * Created by ½ð³É on 2015/9/8.
 */
public class ProxyResponse implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    private String clientId;

    public ProxyResponse(String clientId){
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    @Override
    public String getMessageType() {
        return "ProxyResponse";
    }
}

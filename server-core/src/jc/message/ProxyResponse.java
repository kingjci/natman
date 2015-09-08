package jc.message;

/**
 * Created by ½ð³É on 2015/9/8.
 */
public class ProxyResponse implements Message {

    private String ClientId;

    public String getClientId() {
        return ClientId;
    }

    public void setClientId(String clientId) {
        ClientId = clientId;
    }

    @Override
    public String getMessageType() {
        return "ProxyResponse";
    }
}

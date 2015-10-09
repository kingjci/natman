package jc.message;

import java.io.Serializable;

/**
 * Created by ½ð³É on 2015/9/8.
 */
public class PingResponse implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    private String ip;
    

    @Override
    public String getMessageType() {
        return "PingResponse";
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

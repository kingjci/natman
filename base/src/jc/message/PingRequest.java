package jc.message;

import java.io.Serializable;

/**
 * Created by ��� on 2015/9/8.
 */
public class PingRequest implements Message, Serializable {

    private static final long serialVersionUID = 1L;
    
    @Override
    public String getMessageType() {
        return "PingRequest";
    }
}

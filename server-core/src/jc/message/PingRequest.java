package jc.message;

/**
 * Created by ��� on 2015/9/8.
 */
public class PingRequest implements Message {

    @Override
    public String getMessageType() {
        return "PingRequest";
    }
}

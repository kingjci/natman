package jc.message;

/**
 * Created by ��� on 2015/9/8.
 */
public class PingResponse implements Message {
    @Override
    public String getMessageType() {
        return "PingResponse";
    }
}

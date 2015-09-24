package jc.message;

/**
 * Created by ½ð³É on 2015/9/8.
 */
public class PingRequest implements Message {

    @Override
    public String getMessageType() {
        return "PingRequest";
    }
}

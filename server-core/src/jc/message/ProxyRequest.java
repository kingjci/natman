package jc.message;

/**
 * Created by 金成 on 2015/9/8.
 */
public class ProxyRequest implements Message {
    //这个消息是服务器发送给客户端的，当服务器希望客户端建立一个连接的时候使用

    @Override
    public String getMessageType() {
        return "ProxyRequest";
    }
}

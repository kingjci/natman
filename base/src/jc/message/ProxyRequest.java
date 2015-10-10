package jc.message;

import java.io.Serializable;

/**
 * Created by 金成 on 2015/9/8.
 */
public class ProxyRequest implements Message, Serializable {
    //这个消息是服务器发送给客户端的，当服务器希望客户端建立一个连接的时候使用

    private static final long serialVersionUID = 1L;

    @Override
    public String getMessageType() {
        return "ProxyRequest";
    }
}

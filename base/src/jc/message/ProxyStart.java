package jc.message;

import java.io.Serializable;

/**
 * Created by 金成 on 2015/9/8.
 */
public class ProxyStart implements Message, Serializable{
    /***
     *   这个消息是服务器在代理连接上发送代理请求字节之前发送给客户端的
     */

    private static final long serialVersionUID = 1L;

    private String url; //tcp://jincheng.link:8000服务器上远端端口代理的连接url
    private String publicConnectionAddress; //来自Internet上客户端的网络地址

    public ProxyStart(String url, String publicConnectionAddress){
        this.url = url;
        this.publicConnectionAddress = publicConnectionAddress;
    }

    public String getUrl() {
        return url;
    }

    public String getPublicConnectionAddress() {
        return publicConnectionAddress;
    }

    public void setClientAddrress(String clientAddrress) {
        publicConnectionAddress = clientAddrress;
    }

    @Override
    public String getMessageType() {
        return "ProxyStart";
    }
}

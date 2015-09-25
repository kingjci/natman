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

    private String Url; //tcp://jincheng.link:8000服务器上远端端口代理的连接url
    private String ClientAddress; //来自Internet上客户端的网络地址

    public ProxyStart(String url, String clientAddress){
        this.Url = url;
        this.ClientAddress = clientAddress;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getClientAddress() {
        return ClientAddress;
    }

    public void setClientAddrress(String clientAddrress) {
        ClientAddress = clientAddrress;
    }

    @Override
    public String getMessageType() {
        return "ProxyStart";
    }
}

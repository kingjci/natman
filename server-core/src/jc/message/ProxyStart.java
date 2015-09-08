package jc.message;

/**
 * Created by 金成 on 2015/9/8.
 */
public class ProxyStart implements Message{
    /***
     *   这个消息是服务器在代理连接上发送代理请求字节之前发送给客户端的
     */
    private String Url; //tcp://jincheng.link:8000服务器上远端端口代理的连接url
    private String ClientAddrress; //来自Internet上客户端的网络地址

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getClientAddrress() {
        return ClientAddrress;
    }

    public void setClientAddrress(String clientAddrress) {
        ClientAddrress = clientAddrress;
    }

    @Override
    public String getMessageType() {
        return "ProxyStart";
    }
}

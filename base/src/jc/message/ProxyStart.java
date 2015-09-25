package jc.message;

import java.io.Serializable;

/**
 * Created by ��� on 2015/9/8.
 */
public class ProxyStart implements Message, Serializable{
    /***
     *   �����Ϣ�Ƿ������ڴ��������Ϸ��ʹ��������ֽ�֮ǰ���͸��ͻ��˵�
     */

    private static final long serialVersionUID = 1L;

    private String Url; //tcp://jincheng.link:8000��������Զ�˶˿ڴ��������url
    private String ClientAddress; //����Internet�Ͽͻ��˵������ַ

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

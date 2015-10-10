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

    private String url; //tcp://jincheng.link:8000��������Զ�˶˿ڴ��������url
    private String publicConnectionAddress; //����Internet�Ͽͻ��˵������ַ

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

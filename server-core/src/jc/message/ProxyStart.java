package jc.message;

/**
 * Created by ��� on 2015/9/8.
 */
public class ProxyStart implements Message{
    /***
     *   �����Ϣ�Ƿ������ڴ��������Ϸ��ʹ��������ֽ�֮ǰ���͸��ͻ��˵�
     */
    private String Url; //tcp://jincheng.link:8000��������Զ�˶˿ڴ��������url
    private String ClientAddrress; //����Internet�Ͽͻ��˵������ַ

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

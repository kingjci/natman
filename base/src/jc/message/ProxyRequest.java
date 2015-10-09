package jc.message;

import java.io.Serializable;

/**
 * Created by ��� on 2015/9/8.
 */
public class ProxyRequest implements Message, Serializable {
    //�����Ϣ�Ƿ��������͸��ͻ��˵ģ���������ϣ���ͻ��˽���һ�����ӵ�ʱ��ʹ��

    private static final long serialVersionUID = 1L;

    private String ip;

    @Override
    public String getMessageType() {
        return "ProxyRequest";
    }

    @Override
    public void setIP(String ip) {
        this.ip = ip;
    }

    @Override
    public String getIP() {
        return ip;
    }
}

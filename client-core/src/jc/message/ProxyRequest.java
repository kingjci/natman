package jc.message;

/**
 * Created by ��� on 2015/9/8.
 */
public class ProxyRequest implements Message {
    //�����Ϣ�Ƿ��������͸��ͻ��˵ģ���������ϣ���ͻ��˽���һ�����ӵ�ʱ��ʹ��

    @Override
    public String getMessageType() {
        return "ProxyRequest";
    }
}

package jc.message;

import java.io.Serializable;

/**
 * Created by ��� on 2015/9/8.
 */
public class AuthRequest implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    private float version; //��ǰ�汾
    private String clientId; //���ͻ��˿�ʼһ���µ�session��ʱ��Ϊ��

    public AuthRequest(String clientId, float version){
        this.clientId = clientId;
        this.version = version;

    }


    public float getVersion() {
        return version;
    }

    public boolean isNew(){

        return clientId == null | "".equalsIgnoreCase(clientId);

    }

    public String getClientId() {
        return clientId;
    }

    @Override
    public String getMessageType() {
        return "AuthRequest";
    }

}

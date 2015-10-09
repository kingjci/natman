package jc.message;

import java.io.Serializable;

/**
 * Created by ��� on 2015/9/8.
 */
public class AuthRequest implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    private float Version; //��ǰ�汾
    private float MMversion; //major minor client version
    private String ClientId; //���ͻ��˿�ʼһ���µ�session��ʱ��Ϊ��
    private String IP;

    public AuthRequest(String cliendId, float version, float MMversion){
        this.ClientId = cliendId;
        this.Version = version;
        this.MMversion = MMversion;
    }


    public float getVersion() {
        return Version;
    }

    public void setVersion(float version) {
        Version = version;
    }

    public float getMMversion() {
        return MMversion;
    }

    public void setMMversion(float MMversion) {
        this.MMversion = MMversion;
    }


    public String getClientId() {
        return ClientId;
    }

    public void setClientId(String clientId) {
        ClientId = clientId;
    }

    @Override
    public String getMessageType() {
        return "AuthRequest";
    }

    @Override
    public String getIP() {
        return IP;
    }

    @Override
    public void setIP(String ip) {
        this.IP = ip;
    }
}

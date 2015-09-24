package jc.message;

/**
 * Created by ��� on 2015/9/8.
 */
public class AuthRequest implements Message {

    private float Version; //��ǰ�汾
    private float MMversion; //major minor client version
    private String ClientId; //���ͻ��˿�ʼһ���µ�session��ʱ��Ϊ��

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
}

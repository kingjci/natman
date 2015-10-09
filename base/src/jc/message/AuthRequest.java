package jc.message;

import java.io.Serializable;

/**
 * Created by 金成 on 2015/9/8.
 */
public class AuthRequest implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    private float Version; //当前版本
    private float MMversion; //major minor client version
    private String ClientId; //当客户端开始一个新的session的时候为空
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

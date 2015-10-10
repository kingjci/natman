package jc.message;

import java.io.Serializable;

/**
 * Created by 金成 on 2015/9/8.
 */
public class AuthRequest implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    private float version; //当前版本
    private String clientId; //当客户端开始一个新的session的时候为空

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

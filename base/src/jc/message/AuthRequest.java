package jc.message;

import java.io.Serializable;

/**
 * Created by ��� on 2015/9/8.
 */
public class AuthRequest implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    private final float version;
    private final String clientId;
    private String username;
    private String password;


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

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getMessageType() {
        return "AuthRequest";
    }

}

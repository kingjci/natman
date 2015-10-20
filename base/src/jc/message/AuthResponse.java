package jc.message;

import java.io.Serializable;

public class AuthResponse implements Message, Serializable {

    private static final long serialVersionUID = 1L;
    
    private float version;
    private String clientId;
    private String error;
    private boolean auth;


    public AuthResponse(float version) {
        this.version = version;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void refuse(String error){
        this.auth = false;
        this.error = new String(error);
    }

    public float getVersion() {
        return version;
    }

    public String getClientId() {
        return clientId;
    }

    public String getError() {
        return error;
    }

    public boolean isAuth() {
        return auth;
    }

    @Override
    public String getMessageType() {
        return "AuthResponse";
    }

}

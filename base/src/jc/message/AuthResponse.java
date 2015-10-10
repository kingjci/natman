package jc.message;

import java.io.Serializable;

/**
 * Created by ½ð³É on 2015/9/8.
 */
public class AuthResponse implements Message, Serializable {

    private static final long serialVersionUID = 1L;
    
    private float version;
    private String clientId;
    private boolean reconnect;
    private String error;
    private boolean hasError;

    public AuthResponse(float version, String clientId) {
        this.version = version;
        this.clientId = clientId;
        this.reconnect = false;
    }

    public AuthResponse(String error){
        this.hasError = true;
        this.error = new String(error);
    }

    public boolean isReconnect() {
        return reconnect;
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
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

    public boolean hasError() {
        return hasError;
    }

    @Override
    public String getMessageType() {
        return "AuthResponse";
    }

}

package jc.message;

import java.io.Serializable;

/**
 * Created by ½ð³É on 2015/9/8.
 */
public class AuthResponse implements Message, Serializable {

    private static final long serialVersionUID = 1L;
    
    private float version;
    private String clientId;
    private String error;
    private boolean hasError;
    private String ip;

    public AuthResponse(float version, String clientId) {
        this.version = version;
        this.clientId = clientId;
    }

    public AuthResponse(String error){
        this.hasError = true;
        this.error = error;
    }

    public float getVersion() {
        return version;
    }

    public void setVersion(float version) {
        this.version = version;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String getMessageType() {
        return "AuthResponse";
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

package jc.message;

/**
 * Created by ½ð³É on 2015/9/8.
 */
public class AuthResponse implements Message {

    private float Version;
    private float MMVersion;
    private String ClientId;
    private String Error;

    public AuthResponse(float version, float MMVersion, String clientId) {
        this.Version = version;
        this.MMVersion = MMVersion;
        this.ClientId = clientId;
    }

    public float getVersion() {
        return Version;
    }

    public void setVersion(float version) {
        Version = version;
    }

    public float getMMVersion() {
        return MMVersion;
    }

    public void setMMVersion(float MMVersion) {
        this.MMVersion = MMVersion;
    }

    public String getClientId() {
        return ClientId;
    }

    public void setClientId(String clientId) {
        ClientId = clientId;
    }

    public String getError() {
        return Error;
    }

    public void setError(String error) {
        Error = error;
    }

    @Override
    public String getMessageType() {
        return "AuthResponse";
    }
}

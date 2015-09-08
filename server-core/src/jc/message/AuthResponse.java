package jc.message;

/**
 * Created by ½ð³É on 2015/9/8.
 */
public class AuthResponse implements Message {

    private String Version;
    private String MMVersion;
    private String ClientId;
    private String Error;

    public String getVersion() {
        return Version;
    }

    public void setVersion(String version) {
        Version = version;
    }

    public String getMMVersion() {
        return MMVersion;
    }

    public void setMMVersion(String MMVersion) {
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

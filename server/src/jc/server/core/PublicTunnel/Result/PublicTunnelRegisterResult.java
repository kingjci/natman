package jc.server.core.PublicTunnel.Result;

public class PublicTunnelRegisterResult {

    private String state;   //success or failure
    private String error;

    private String publicUrl;
    private int remotePort; //the actual port assigned by the server

    public String getState() {
        return state;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.state = "failure";
        this.error = error;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
}


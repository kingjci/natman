package jc.client.core;

import java.util.Map;

/**
 * Created by ½ð³É on 2015/9/23.
 */
public class Config {

    private String httpProxy;
    private String serverAddr;
    private String authToken;
    private Map<String, TunnelConfiguration> tunnels;

    public String getHttpProxy() {
        return httpProxy;
    }

    public void setHttpProxy(String httpProxy) {
        this.httpProxy = httpProxy;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Map<String, TunnelConfiguration> getTunnels() {
        return tunnels;
    }

    public void setTunnels(Map<String, TunnelConfiguration> tunnels) {
        this.tunnels = tunnels;
    }
}

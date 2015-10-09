package jc.client.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ½ð³É on 2015/9/23.
 */
public class Config {

    private String httpProxy;
    private String serverAddr = "127.0.0.1";
    private String authToken;
    private String command;
    private Map<String, TunnelConfiguration> tunnels = new HashMap<String, TunnelConfiguration>();



    public String getHttpProxy() {
        return httpProxy;
    }


    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
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

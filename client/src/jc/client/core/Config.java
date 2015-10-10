package jc.client.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ½ð³É on 2015/9/23.
 */
public class Config {

    private String serverAddress = "127.0.0.1";
    private String command;
    private Map<String, PublicTunnelConfiguration> publicTunnelConfiguration = new HashMap<String, PublicTunnelConfiguration>();

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public Map<String, PublicTunnelConfiguration> getPublicTunnelConfiguration() {
        return publicTunnelConfiguration;
    }

    public void setPublicTunnelConfiguration(Map<String, PublicTunnelConfiguration> publicTunnelConfiguration) {
        this.publicTunnelConfiguration = publicTunnelConfiguration;
    }
}

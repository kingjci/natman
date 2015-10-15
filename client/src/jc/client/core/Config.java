package jc.client.core;

import java.util.HashMap;
import java.util.Map;

public class Config {

    private String serverAddress = "127.0.0.1";
    private String clientAddress = "127.0.0.1";
    private String command;
    private String username;
    private String password;

    private Map<String, PublicTunnelConfiguration> publicTunnelConfigurations = new HashMap<String, PublicTunnelConfiguration>();

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

    public Map<String, PublicTunnelConfiguration> getPublicTunnelConfigurations() {
        return publicTunnelConfigurations;
    }

    public void putPublicTunnelConfiguration( PublicTunnelConfiguration publicTunnelConfiguration){
        publicTunnelConfigurations.put(
                publicTunnelConfiguration.getName(),
                publicTunnelConfiguration
        );
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

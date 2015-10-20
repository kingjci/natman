package jc.client.core;

public class PublicTunnelConfiguration {

    private String name;
    private String subDomain; // this is useful when http port is used
    private String protocol; //tcp udp http
    private int remotePort; // public tunnel
    private int localPort; // private tunnel

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getSubDomain() {
        return subDomain == null ? "": subDomain;
    }

    public void setSubDomain(String subDomain) {
        this.subDomain = subDomain;
    }



    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package jc.client.core;

import java.util.Map;

/**
 * Created by ½ð³É on 2015/9/23.
 */
public class TunnelConfiguration {

    private String subDomian;
    private String hostName;
    private Map<String, String> protocols;
    private int remotePort;
    private int localPort;

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getSubDomian() {
        return subDomian;
    }

    public void setSubDomian(String subDomian) {
        this.subDomian = subDomian;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
}

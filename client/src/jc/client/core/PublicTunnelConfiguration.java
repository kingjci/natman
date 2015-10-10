package jc.client.core;

import java.util.Map;

/**
 * Created by ��� on 2015/9/23.
 */
public class PublicTunnelConfiguration {

    //����ļ��Ǵ������ļ����������в�����ȡ�� ������public tunnel�� private��ӳ���ϵ
    private String subDomain; // this is useful when http port is used
    private String hostName; // server domain or ip
    private Map<String, String> protocols; //tcp udp http
    private int remotePort; // public tunnel
    private int localPort; // private tunnel

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

    public String getSubDomain() {
        return subDomain;
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
}

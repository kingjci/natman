package jc.client.core;

import java.util.Map;

/**
 * Created by ��� on 2015/9/23.
 */
public class TunnelConfiguration {

    private String subDomian;
    private String hostName;
    private Map<String, String> protocols;
    private int remotePort;

    public String getSubDomian() {
        return subDomian;
    }

    public void setSubDomian(String subDomian) {
        this.subDomian = subDomian;
    }
}

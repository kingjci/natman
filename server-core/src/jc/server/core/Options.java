package jc.server.core;

/**
 * Created by ½ð³É on 2015/9/8.
 */
public class Options {

    private String httpAddress;
    private String httpsAddress;
    private String tunnelAddress;
    private String domain;
    private String tlsCrt;
    private String logTo;
    private String logLevel;

    public String getHttpAddress() {
        return httpAddress;
    }

    public void setHttpAddress(String httpAddress) {
        this.httpAddress = httpAddress;
    }

    public String getHttpsAddress() {
        return httpsAddress;
    }

    public void setHttpsAddress(String httpsAddress) {
        this.httpsAddress = httpsAddress;
    }

    public String getTunnelAddress() {
        return tunnelAddress;
    }

    public void setTunnelAddress(String tunnelAddress) {
        this.tunnelAddress = tunnelAddress;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTlsCrt() {
        return tlsCrt;
    }

    public void setTlsCrt(String tlsCrt) {
        this.tlsCrt = tlsCrt;
    }

    public String getLogTo() {
        return logTo;
    }

    public void setLogTo(String logTo) {
        this.logTo = logTo;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
}

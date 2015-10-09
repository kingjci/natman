package jc.client.core;

/**
 * Created by ½ð³É on 2015/9/23.
 */
public class PrivateTunnel {

    private String publicUrl;
    private String protocol;
    private String localAddress = "127.0.0.1";
    private int localPort;

    public PrivateTunnel(String publicUrl, int localPort){
        this.publicUrl = publicUrl;
        this.localPort = localPort;
    }

    public PrivateTunnel(String publicUrl, String localAddress, int localPort, String protocol){
        this.publicUrl = publicUrl;
        this.localAddress = localAddress;
        this.localPort = localPort;
        this.protocol = protocol;
    }


    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }
}

package jc.client.core;

/**
 * Created by ��� on 2015/9/23.
 */
public class PrivateTunnel {

    private String protocol;
    private String publicUrl;
    private String localAddress = "127.0.0.1";
    private int localPort;

    public PrivateTunnel(String publicUrl,String localAddress, int localPort, String protocol){
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

    public String getPublicUrl() {
        return publicUrl;
    }
}

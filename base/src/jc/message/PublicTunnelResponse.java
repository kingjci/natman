package jc.message;

import java.io.Serializable;

/**
 * Created by ��� on 2015/9/8.
 */
public class PublicTunnelResponse implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    private String publicUrl; // tcp://jincheng.link:8000
    private String protocol;
    private int localPort;
    private String error;

    public PublicTunnelResponse(String error){
        this.error = new String(error);
    }


    public PublicTunnelResponse(String publicUrl, String protocol,int localPort){
        this.publicUrl = publicUrl;
        this.protocol = protocol;
        this.localPort = localPort;
    }

    public int getLocalPort() {
        return localPort;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getError() {
        return error;
    }

    public boolean hasError(){

        return (error != null) && (!"".equalsIgnoreCase(error));
    }

    @Override
    public String getMessageType() {
        return "PublicTunnelResponse";
    }
}

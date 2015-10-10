package jc.message;

import java.io.Serializable;

/**
 * Created by ��� on 2015/9/8.
 */
public class PublicTunnelResponse implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    private String RequestId; //��TunnelRequest�е�RequestId��ͬ
    private String Url; // tcp://jincheng.link:8000
    private String Protocol;
    private int LocalPort;
    private String Error;

    public PublicTunnelResponse(String error){
        this.Error = new String(error);//���ִ���Ĺ��췽ʽ
    }


    public PublicTunnelResponse(String url, String protocol, String requestId, int localPort){
        this.Url = url;
        this.Protocol = protocol;
        this.RequestId = requestId;
        this.LocalPort = localPort;
    }

    public int getLocalPort() {
        return LocalPort;
    }

    public String getUrl() {
        return Url;
    }

    public String getProtocol() {
        return Protocol;
    }

    public String getError() {
        return Error;
    }

    public boolean hasError(){

        return (Error != null) && (!"".equalsIgnoreCase(Error));
    }

    @Override
    public String getMessageType() {
        return "TunnelResponse";
    }
}

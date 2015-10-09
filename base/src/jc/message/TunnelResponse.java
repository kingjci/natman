package jc.message;

import java.io.Serializable;

/**
 * Created by 金成 on 2015/9/8.
 */
public class TunnelResponse implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    private String RequestId; //与TunnelRequest中的RequestId相同
    private String Url; // tcp://jincheng.link:8000
    private String Protocol;
    private int LocalPort;
    private String Error = "";

    private String ip;

    public TunnelResponse(String error){
        this.Error = error;//出现错误的构造方式
    }


    public TunnelResponse(String url, String protocol, String requestId, int localPort){
        this.Url = url;
        this.Protocol = protocol;
        this.RequestId = requestId;
        this.LocalPort = localPort;
    }


    public String getRequestId() {
        return RequestId;
    }

    public void setRequestId(String requestId) {
        RequestId = requestId;
    }

    public int getLocalPort() {
        return LocalPort;
    }

    public void setLocalPort(int port) {
        LocalPort = port;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getProtocol() {
        return Protocol;
    }

    public void setProtocol(String protocol) {
        Protocol = protocol;
    }

    public String getError() {
        return Error;
    }

    public void setError(String error) {
        Error = error;
    }

    public boolean hasError(){

        return !"".equalsIgnoreCase(Error);
    }

    @Override
    public String getMessageType() {
        return "TunnelResponse";
    }

    @Override
    public void setIP(String ip) {
        this.ip = ip;
    }

    @Override
    public String getIP() {
        return ip;
    }
}

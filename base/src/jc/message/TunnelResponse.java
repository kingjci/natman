package jc.message;

import java.io.Serializable;

/**
 * Created by ��� on 2015/9/8.
 */
public class TunnelResponse implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    private String RequestId; //��TunnelRequest�е�RequestId��ͬ
    private String Url; // tcp://jincheng.link:8000
    private String Protocol;
    private String Error;

    public TunnelResponse(String error){
        this.Error = error;//���ִ���Ĺ��췽ʽ
    }


    public TunnelResponse(String url, String protocol, String requestId){
        this.Url = url;
        this.Protocol = protocol;
        this.RequestId = requestId;
    }


    public String getRequestId() {
        return RequestId;
    }

    public void setRequestId(String requestId) {
        RequestId = requestId;
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

    @Override
    public String getMessageType() {
        return "TunnelResponse";
    }
}

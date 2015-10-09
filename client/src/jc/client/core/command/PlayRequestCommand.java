package jc.client.core.command;

import jc.client.core.PrivateTunnel;

/**
 * Created by ½ð³É on 2015/9/23.
 */
public class PlayRequestCommand implements Command {

    private PrivateTunnel tunnel;
    private byte[] payload;


    public PlayRequestCommand(PrivateTunnel tunnel, byte[] payload){
        this.tunnel = tunnel;
        this.payload = payload;
    }

    public PrivateTunnel getTunnel() {
        return tunnel;
    }

    public void setTunnel(PrivateTunnel tunnel) {
        this.tunnel = tunnel;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    @Override
    public String getCommandType() {
        return "PlayRequestCommand";
    }
}

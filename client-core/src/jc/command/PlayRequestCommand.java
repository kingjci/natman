package jc.command;

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

    @Override
    public String getCommandType() {
        return "PlayRequestCommand";
    }
}

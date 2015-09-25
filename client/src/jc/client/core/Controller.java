package jc.client.core;

import jc.command.Command;
import jc.command.PlayRequestCommand;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import static jc.client.core.Utils.Go;

/**
 * Created by ½ð³É on 2015/9/23.
 */
public class Controller implements Runnable{

    private ControlConnection controlConnection;
    private BlockingQueue<Command> cmds;
    private Config config;

    public Controller(Config config){

        cmds = new LinkedBlockingQueue<Command>();
        controlConnection = new ControlConnection( this, config);
    }

    public void playRequest(PrivateTunnel tunnel, byte[] payload){

        PlayRequestCommand playRequestCommand = new PlayRequestCommand(tunnel, payload);
        try {
            cmds.put(playRequestCommand);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }

    public ControlConnection getControlConnection(){
        return this.controlConnection;
    }

    @Override
    public void run() {




    }
}

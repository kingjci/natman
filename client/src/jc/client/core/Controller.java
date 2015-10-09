package jc.client.core;

import jc.Connection;
import jc.client.core.command.Command;
import jc.client.core.command.PlayRequestCommand;
import jc.client.core.command.QuitCommand;

import static jc.client.core.Utils.Dial;
import static jc.client.core.Utils.Go;
import static jc.client.core.Utils.timeStamp;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by ��� on 2015/9/23.
 */
public class Controller implements Runnable{

    private ControlConnection controlConnection;
    private BlockingQueue<Command> cmds;
    private Config config;

    public Controller(Config config){

        this.cmds = new LinkedBlockingQueue<Command>();
        this.controlConnection = new ControlConnection( this, config);
    }


    public ControlConnection getControlConnection(){
        return this.controlConnection;
    }

    @Override
    public void run() {

        Go(this.controlConnection);

        while (true){
            try{
                Command command = cmds.take();
                switch (command.getCommandType()){

                    //不清楚这个指令的作用
                    case "PlayRequestCommand":

                        PlayRequestCommand playRequestCommand =
                                (PlayRequestCommand) command;
                        Go(new PlayRequest(playRequestCommand.getTunnel(), playRequestCommand.getPayload()));

                        break;

                    case "QuitCommand":

                        QuitCommand quitCommand = (QuitCommand) command;
                        controlConnection.shutDown("unknown");
                        System.out.printf("[%s][Controller]QuitCommand\n", timeStamp());
                        //shutdown
                        return;



                    default:

                        System.out.printf("[%s][Controller]unknown command\n", timeStamp());

                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }

        }

    }


}

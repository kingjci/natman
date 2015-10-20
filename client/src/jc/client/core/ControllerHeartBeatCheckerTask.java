package jc.client.core;

import jc.TCPConnection;
import jc.Time;
import jc.command.Command;
import jc.command.QuitCommand;
import org.apache.log4j.Logger;

import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Created by 金成 on 2015/10/15.
 */

public class ControllerHeartBeatCheckerTask extends TimerTask {

    private Time lastPingResponse;
    private Option option;
    private Logger runtimeLogger;
    private Logger accessLogger;
    private TCPConnection tcpConnection;
    private BlockingQueue<Command> commands;


    public ControllerHeartBeatCheckerTask(Controller controller){

        tcpConnection = controller.getTcpConnection();
        lastPingResponse = controller.getLastPingResponse();
        option = controller.getOption();
        commands = controller.getCommands();
        runtimeLogger = controller.getRuntimeLogger();
        accessLogger = controller.getAccessLogger();

    }



    @Override
    public void run() {

        boolean needPingResponse = System.currentTimeMillis() - lastPingResponse.getTime() > 30*1000;

        if (needPingResponse){

            try{
                commands.put(new QuitCommand("ControllerHeartBeatCheckerTask","Lost heartbeat",-5));
            }catch (InterruptedException e){
                runtimeLogger.error(e.getMessage(),e);
            }

            runtimeLogger.error(
                    String.format(
                            "Lost heartbeat from %s, control restart in %d seconds",
                            tcpConnection.getRemoteAddress(),
                            option.getWaitTime()/1000
                    )
            );

        }

    }
}

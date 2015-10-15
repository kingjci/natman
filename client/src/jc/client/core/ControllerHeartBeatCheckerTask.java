package jc.client.core;

import jc.TCPConnection;
import jc.Time;
import org.apache.log4j.Logger;

import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

/**
 * Created by 金成 on 2015/10/15.
 */

public class ControllerHeartBeatCheckerTask extends TimerTask {

    private final Time lastPingResponse;
    private final Option option;
    private final CountDownLatch latch;
    private final Logger runtimeLogger;
    private final Logger accessLogger;
    private final TCPConnection tcpConnection;


    public ControllerHeartBeatCheckerTask(ControllerHeartBeat controllerHeartBeat){

        tcpConnection = controllerHeartBeat.getTcpConnection();
        lastPingResponse = controllerHeartBeat.getLastPingResponse();
        option = controllerHeartBeat.getOption();
        latch = controllerHeartBeat.getLatch();
        runtimeLogger = controllerHeartBeat.getRuntimeLogger();
        accessLogger = controllerHeartBeat.getAccessLogger();
    }



    @Override
    public void run() {

        boolean needPingResponse = System.currentTimeMillis() - lastPingResponse.getTime() > 30*1000;

        if (needPingResponse){

            latch.countDown();

            runtimeLogger.error(
                    String.format(
                            "Lost heartbeat from %s, control restart in %d seconds",
                            tcpConnection.getRemoteAddress(),
                            option.getWaitTime()
                    )
            );

        }

    }
}

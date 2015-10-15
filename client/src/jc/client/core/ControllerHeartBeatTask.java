package jc.client.core;

import jc.TCPConnection;
import jc.Time;
import jc.message.PingRequest;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.TimerTask;

/**
 * Created by 金成 on 2015/10/15.
 */
public class ControllerHeartBeatTask extends TimerTask {

    private final String clientId;
    private final TCPConnection tcpConnection;
    private final Time lastPing;
    private final Logger runtimeLogger;
    private final Logger accessLogger;


    public ControllerHeartBeatTask(ControllerHeartBeat controllerHeartBeat){
        clientId = controllerHeartBeat.getClientId();
        tcpConnection = controllerHeartBeat.getTcpConnection();
        lastPing = controllerHeartBeat.getLastPing();
        runtimeLogger = controllerHeartBeat.getRuntimeLogger();
        accessLogger = controllerHeartBeat.getAccessLogger();
    }


    @Override
    public void run() {

        PingRequest pingRequest = new PingRequest(clientId);

        try {
            tcpConnection.writeMessage(pingRequest);
            lastPing.setTime(System.currentTimeMillis());
            runtimeLogger.debug(
                    String.format("Ping %s successfully", tcpConnection.getRemoteAddress())
            );

        }catch (IOException e){

            runtimeLogger.error(String.format("Ping %s failure", tcpConnection.getRemoteAddress()));
            runtimeLogger.error(e.getMessage(), e);
        }




    }
}



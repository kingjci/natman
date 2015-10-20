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


    public ControllerHeartBeatTask(Controller controller){
        clientId = controller.getClientId();
        tcpConnection = controller.getTcpConnection();
        lastPing = controller.getLastPing();
        runtimeLogger = controller.getRuntimeLogger();
        accessLogger = controller.getAccessLogger();
    }


    @Override
    public void run() {

        PingRequest pingRequest = new PingRequest(clientId);

        try {
            tcpConnection.writeMessage(pingRequest);
            lastPing.setTime(System.currentTimeMillis());
            runtimeLogger.debug(
                    String.format(
                            "Ping %s[%s] successfully",
                            tcpConnection.getRemoteAddress(),
                            tcpConnection.getConnectionId()
                    )
            );

        }catch (IOException e){

            runtimeLogger.error(
                    String.format(
                            "Ping %s[%s] failure",
                            tcpConnection.getRemoteAddress(),
                            tcpConnection.getConnectionId()
                    )
            );
            //runtimeLogger.error(e.getMessage(), e);
        }
    }
}



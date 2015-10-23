package jc.server.core.ControlConnection;

import jc.TCPConnection;
import jc.Time;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.TimerTask;

public class ControlConnectionHeartBeatChecker extends TimerTask {

    private Time lastPing;
    private final TCPConnection tcpConnection;
    private final Logger runtimeLogger;
    private final Logger accessLogger;
    private final ControlConnection controlConnection;

    public ControlConnectionHeartBeatChecker(
            ControlConnection controlConnection
    ){
        this.tcpConnection = controlConnection.getTcpConnection();
        this.lastPing = controlConnection.getLastPing();
        this.runtimeLogger = controlConnection.getRuntimeLogger();
        this.accessLogger = controlConnection.getAccessLogger();
        this.controlConnection = controlConnection;
    }

    @Override
    public void run() {
        long currentTimeMillis = System.currentTimeMillis();
        long latency  = currentTimeMillis - lastPing.getTime();
        if (latency > 30*1000){
            runtimeLogger.error(
                    String.format("Lost heartbeat from %s[%s]",
                            tcpConnection.getRemoteAddress(),
                            tcpConnection.getConnectionId()
                    )
            );
            try{
                controlConnection.close();
            }catch (IOException e){
                runtimeLogger.error(e.getMessage(),e);
            }
        }
    }
}
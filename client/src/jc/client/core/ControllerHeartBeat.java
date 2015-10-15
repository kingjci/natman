package jc.client.core;

import jc.Time;
import jc.TCPConnection;
import org.apache.log4j.Logger;

import java.util.Timer;
import java.util.concurrent.CountDownLatch;

public class ControllerHeartBeat implements Runnable{

    private final Time lastPing;
    private final Time lastPingResponse;
    private final TCPConnection tcpConnection;
    private final String clientId;
    private final CountDownLatch latch;

    private final Config config;
    private final Option option;
    private final Logger runtimeLogger;
    private final Logger accessLogger;

    private Timer heartBeatTimer;
    private Timer heartBeatResponseCheckTimer;

    public ControllerHeartBeat(Controller controller){

        clientId = controller.getClientId();
        config = controller.getConfig();
        option = controller.getOption();
        runtimeLogger = controller.getRuntimeLogger();
        accessLogger = controller.getAccessLogger();

        lastPingResponse = controller.getLastPingResponse();
        lastPing = new Time(lastPingResponse.getTime() - option.getHeartBeatCheckerInterval());
        tcpConnection = controller.getTcpConnection();
        heartBeatTimer = new Timer();
        heartBeatResponseCheckTimer = new Timer();
        latch = new CountDownLatch(1);

    }

    public Time getLastPing() {
        return lastPing;
    }

    public Time getLastPingResponse() {
        return lastPingResponse;
    }

    public TCPConnection getTcpConnection() {
        return tcpConnection;
    }

    public Config getConfig() {
        return config;
    }

    public Option getOption() {
        return option;
    }

    public Logger getRuntimeLogger() {
        return runtimeLogger;
    }

    public Logger getAccessLogger() {
        return accessLogger;
    }

    public String getClientId() {
        return clientId;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    @Override
    public void run() {

        try{

            this.heartBeatTimer.schedule(new ControllerHeartBeatTask(this), 0, option.getHeartBeatInterval());
            this.heartBeatResponseCheckTimer.schedule(new ControllerHeartBeatCheckerTask(this), 0, option.getHeartBeatCheckerInterval());

            latch.await();

            heartBeatTimer.cancel();
            heartBeatResponseCheckTimer.cancel();


        }catch (InterruptedException e){
            runtimeLogger.error(e.getMessage(),e);
        }
        
    }
}

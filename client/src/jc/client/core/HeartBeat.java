package jc.client.core;

import jc.message.PingRequest;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ½ð³É on 2015/9/24.
 */
public class HeartBeat implements Runnable{

    protected long lastPing;
    protected Long lastPingResponse;
    protected Connection connection;
    protected Object outOfTime;

    private Timer pingTimer;
    private Timer pingResponseCheckTimer;

    public HeartBeat(Long lastPingResponse, Connection connection){
        this.lastPingResponse = lastPingResponse;
        this.lastPing = lastPingResponse - 1*1000;
        this.connection = connection;
        this.pingTimer = new Timer();

        this.pingResponseCheckTimer = new Timer();

    }



    private class PingTimerTask extends TimerTask{



        @Override
        public void run() {

            PingRequest pingRequest = new PingRequest();

            connection.writeMessage(pingRequest);

            lastPing = System.currentTimeMillis();

        }
    }

    private class PingResponseCheckTimerTask extends TimerTask{

        @Override
        public void run() {
            boolean needPingResponse = lastPing - lastPingResponse > 0;

            long pingResponseLatency = System.currentTimeMillis() - lastPing;


            if (needPingResponse && pingResponseLatency > 15*1000){

                System.out.printf("Last ping: %s, Last pong: %s\n", lastPing, lastPingResponse);

                outOfTime.notifyAll();
                return;

            }
        }
    }




    @Override
    public void run() {

        try{
            this.pingTimer.schedule(new PingTimerTask(), 20 * 1000);
            this.pingResponseCheckTimer.schedule(new PingResponseCheckTimerTask(), 1 * 1000);
            outOfTime.wait();

            this.pingTimer.cancel();
            this.pingResponseCheckTimer.cancel();

            System.out.println("error:heart exit");
        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }
}

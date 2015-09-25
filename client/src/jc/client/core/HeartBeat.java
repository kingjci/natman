package jc.client.core;

import jc.Connection;
import jc.message.PingRequest;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ½ð³É on 2015/9/24.
 */
public class HeartBeat implements Runnable{

    protected long lastPing;
    protected Time lastPingResponse;
    protected Connection connection;
    final protected Object outOfTime;

    private Timer pingTimer;
    private Timer pingResponseCheckTimer;

    public HeartBeat(Time lastPingResponse, Connection connection){
        this.lastPingResponse = lastPingResponse;
        this.lastPing = lastPingResponse.getTime() - 1*1000;
        this.connection = connection;
        this.pingTimer = new Timer();
        this.pingResponseCheckTimer = new Timer();
        this.outOfTime = new Object();

    }



    private class PingTimerTask extends TimerTask{

        @Override
        public void run() {

            PingRequest pingRequest = new PingRequest();

            try {
                connection.writeMessage(pingRequest);
                lastPing = System.currentTimeMillis();
                System.out.println("ping:" + lastPing);
            }catch (IOException e){

                System.out.printf("ping %s failure", connection.getRemoteAddr());
            }




        }
    }

    private class PingResponseCheckTimerTask extends TimerTask{

        @Override
        public void run() {

            boolean needPing = System.currentTimeMillis() - lastPing > 30*1000;


            if (needPing){

                System.out.printf("Last ping: %s, Last pong: %s\n", lastPing, lastPingResponse);

                synchronized (outOfTime){
                    outOfTime.notifyAll();
                    System.out.println("error:heartbeat exit");
                }

                return;

            }

        }
    }




    @Override
    public void run() {

        try{

            this.pingTimer.schedule(new PingTimerTask(),0 , 15 * 1000);
            this.pingResponseCheckTimer.schedule(new PingResponseCheckTimerTask(), 0, 1000);

            synchronized (outOfTime){
                outOfTime.wait();
            }


            this.pingTimer.cancel();
            this.pingResponseCheckTimer.cancel();


        }catch (InterruptedException e){
            e.printStackTrace();
        }
        
    }
}

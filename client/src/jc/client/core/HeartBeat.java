package jc.client.core;

import jc.Time;
import jc.message.PingRequest;
import jc.TCPConnection;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static jc.client.core.Utils.timeStamp;

/**
 * Created by ½ð³É on 2015/9/24.
 */
public class HeartBeat implements Runnable{

    protected long lastPing;
    protected Time lastPingResponse;
    protected TCPConnection TCPConnection;
    final protected Object outOfTime;

    private ControlConnection controlConnection;

    private Timer pingTimer;
    private Timer pingResponseCheckTimer;

    public HeartBeat(Time lastPingResponse, TCPConnection TCPConnection, ControlConnection controlConnection){
        this.controlConnection = controlConnection;
        this.lastPingResponse = lastPingResponse;
        this.lastPing = lastPingResponse.getTime() - 1*1000;
        this.TCPConnection = TCPConnection;
        this.pingTimer = new Timer();
        this.pingResponseCheckTimer = new Timer();
        this.outOfTime = new Object();

    }



    private class PingTimerTask extends TimerTask{

        @Override
        public void run() {

            PingRequest pingRequest = new PingRequest(controlConnection.getClientId(), System.currentTimeMillis());

            try {
                TCPConnection.writeMessage(pingRequest);
                lastPing = System.currentTimeMillis();
                System.out.printf("[%s][HeartBeat]ping %s successfully\n",
                        timeStamp(lastPing), TCPConnection.getRemoteAddr());
            }catch (IOException e){

                System.out.printf("[%s][HeartBeat]ping %s failure\n",
                        timeStamp(), TCPConnection.getRemoteAddr());
            }




        }
    }

    private class PingResponseCheckTimerTask extends TimerTask{

        @Override
        public void run() {

            boolean needPingResponse = System.currentTimeMillis() - lastPingResponse.getTime() > 30*1000;

            boolean doNotGetPingResponseFromLastPing = System.currentTimeMillis() - lastPing > 15*1000;

            if (needPingResponse && doNotGetPingResponseFromLastPing){



                synchronized (outOfTime){
                    outOfTime.notifyAll();
                    System.out.printf("[%s][HeartBeat]Last ping: %s, Last pong: %s\n",
                            timeStamp(), timeStamp(lastPing), timeStamp(lastPingResponse.getTime()));
                    System.out.printf("[%s][HeartBeat]Error:heartbeat exit\n", timeStamp());
                }

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

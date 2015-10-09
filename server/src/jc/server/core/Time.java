package jc.server.core;

/**
 * Created by ½ð³É on 2015/9/24.
 */
public class Time {

    private long time;

    public Time(long time){
        this.time = time;
    }

    public Time(){
        this.time = System.currentTimeMillis();
    }

    public long getTime(){
        return time;
    }

    public void setTime(long currentTime){
        this.time = currentTime;
    }

}

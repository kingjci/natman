package jc.client.core;

/**
 * Created by 金成 on 2015/10/15.
 */
public class Option {

    private final float version = 1.0f;
    private final int waitTime = 5*1000; // 5s
    private final int controlPort = 12345;
    private final int heartBeatInterval = 15*1000; // 15s
    private final int heartBeatCheckerInterval = 1*1000; // 1s
    private final int maxLatency = 30*1000; // 30s
    private final int maxWaitCount = 5;


    public Option(){

    }

    public float getVersion() {
        return version;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public int getControlPort() {
        return controlPort;
    }

    public int getHeartBeatInterval() {
        return heartBeatInterval;
    }

    public int getHeartBeatCheckerInterval() {
        return heartBeatCheckerInterval;
    }

    public int getMaxLatency() {
        return maxLatency;
    }

    public int getMaxWaitCount() {
        return maxWaitCount;
    }
}

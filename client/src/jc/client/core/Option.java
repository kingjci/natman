package jc.client.core;

public class Option {

    private float version = 1.0f;
    private int waitTime = 5*1000; // 5s

    private int heartBeatInterval = 15*1000; // 15s
    private int heartBeatCheckerInterval = 1*1000; // 1s
    private int maxLatency = 30*1000; // 30s
    private int maxWaitCount = 5;

    public Option(){

    }

    public float getVersion() {
        return version;
    }

    public int getWaitTime() {
        return waitTime;
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

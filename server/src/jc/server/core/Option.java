package jc.server.core;

public class Option {

    //Store const config of the server
    private final float version = 1.0f;
    private final float minVersion = 0.0f;
    private final int maxGetProxyTime = 5;  // 5s

    public Option(){

    }

    public float getMinVersion() {
        return minVersion;
    }

    public float getVersion() {
        return version;
    }

    public int getMaxGetProxyTime() {
        return maxGetProxyTime;
    }
}

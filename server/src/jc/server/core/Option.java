package jc.server.core;

/**
 * Created by 金成 on 2015/10/15.
 */
public class Option {

    //用来保存服务器中的常量

    private final int controlPort = 12345;

    private final float version = 1.0f;

    private final float minVersion = 0.0f;

    public Option(){

    }


    public int getControlPort() {
        return controlPort;
    }

    public float getMinVersion() {
        return minVersion;
    }

    public float getVersion() {
        return version;
    }
}

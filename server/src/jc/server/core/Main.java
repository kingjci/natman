package jc.server.core;

import jc.Random;
import jc.server.core.ControlTunnel.ControlTunnel;
import jc.server.core.registry.ControlConnectionRegistry;
import jc.server.core.registry.ThreadRegistry;
import jc.server.core.registry.TunnelRegistry;

import java.text.SimpleDateFormat;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static jc.server.core.Utils.Go;


public class Main {

    public static int connReadTimeout = 10*1000;
    public static Options options = new Options();
    public static Random random = new Random();
    public static TunnelRegistry tunnelRegistry = new TunnelRegistry();
    public static ControlConnectionRegistry controlConnectionRegistry = new ControlConnectionRegistry();

    public static ThreadRegistry threadRegistry = new ThreadRegistry();
    public static ShutDown shutDown = new ShutDown(threadRegistry);

    public static Lock consoleLock = new ReentrantLock(false);

    public static SimpleDateFormat timeFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static void main(String[] args) {


        Go(new ControlTunnel(12345));

    }

}

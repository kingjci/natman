package jc.client.core;

import jc.Random;

import java.text.SimpleDateFormat;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static jc.client.core.Utils.Console;
import static jc.client.core.Utils.Go;
import static jc.client.core.Utils.LoadConfiguration;

/**
 * Created by 金成 on 2015/9/5.
 */
public class Main {


    public static Random random = new Random();

    public static ThreadRegistry threadRegistry = new ThreadRegistry();

    public static ShutDown shutDown = new ShutDown(threadRegistry);

    public static Lock consoleLock = new ReentrantLock(false);

    public static Config config = new Config();

    public static SimpleDateFormat timeFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {

        //解析参数
        LoadConfiguration(args);

        Controller controller = new Controller(config);

        Go(controller);

        shutDown.waitForShutDown();

    }
}

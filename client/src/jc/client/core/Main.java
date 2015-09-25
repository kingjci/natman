package jc.client.core;

import jc.Random;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ½ð³É on 2015/9/5.
 */
public class Main {


    public static Random random = new Random();

    public static ThreadRegistry threadRegistry = new ThreadRegistry();

    public static ShutDown shutDown = new ShutDown(threadRegistry);

    public static Lock consoleLock = new ReentrantLock(false);

    public static Config config = new Config();

    public static void main(String[] args) {



        shutDown.waitForShutDown();

        for (int i = 0; i < 100 ; i++){
            Thread thread = null;
            try{
                thread = new testthread(String.valueOf(i));
                thread.start();
            }catch (ThreadNameConflictException e){
                e.printStackTrace();
            }



        }



        try{
            Thread.sleep(1000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        shutDown.shutDown(-2);

    }
}

package jc.server.core;

import jc.TCPConnection;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import static jc.server.core.Main.consoleLock;
import static jc.server.core.Main.threadRegistry;
import static jc.server.core.Main.random;
import static jc.server.core.Main.timeFormat;

/**
 * Created by 金成 on 2015/9/8.
 */
public class Utils {



    public static void Go(Runnable runnable){

        Thread thread = new Thread(runnable);
        thread.start();

    }

    public static void Go(String threadName, Runnable runnable){

        Thread thread = new Thread(runnable);
        threadRegistry.register(threadName,thread);
        thread.start();
    }

    public static void Join(TCPConnection c1, TCPConnection c2){

        //这个程序会等到两个Connection的双向传输都传送结束才结束返回
        CountDownLatch waitGroup = new CountDownLatch(2);
        Go(new Pipe(c1, c2, waitGroup));
        Go(new Pipe(c2, c1, waitGroup));

        System.out.printf("[%s][Utils]Join %s with %s\n", timeStamp(),c1.Id(), c2.Id());

        try{
            waitGroup.await();
            c1.close();
            c2.close();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        System.out.printf("[%s][Utils]Disjoin %s with %s\n", timeStamp(),c1.Id(), c2.Id());

    }


    public static void Console(Object... args){

        consoleLock.lock();

        Object[] objects = new Object[args.length-1];
        System.arraycopy(args, 1, objects, 0, args.length - 1);
        System.out.printf(args[0] + "\n", objects);

        consoleLock.unlock();
    }

    public static String timeStamp(long timeMillis){

        return timeFormat.format(timeMillis);

    }

    public static String timeStamp(){

        return timeStamp(System.currentTimeMillis());
    }


}

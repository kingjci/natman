package jc.client.core;

import static jc.client.core.Utils.Console;
import static jc.client.core.Utils.ShutdownThread;

import static jc.client.core.Main.threadRegistry;
import static jc.client.core.Main.consoleLock;



/**
 * Created by ½ð³É on 2015/9/25.
 */
public class testthread extends Thread {

    private String threadName;

    public testthread(String threadName) throws ThreadNameConflictException {

        if (!threadRegistry.register(threadName, this)){
            throw new ThreadNameConflictException(threadName);
        }
        this.threadName = threadName;
    }

    @Override
    public void run() {



        while (true){

            if (threadRegistry.isShutDown()){

                ShutdownThread(threadName);

                break;
            }

            Console("working thread:%s", threadName);

            try{
                Thread.sleep(10);
            }catch (InterruptedException e){

                ShutdownThread(threadName);
                break;

            }

        }


    }
}

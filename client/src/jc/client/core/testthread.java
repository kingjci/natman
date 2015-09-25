package jc.client.core;

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

                threadRegistry.getWriteLock();
                consoleLock.lock();
                int result = threadRegistry.remove(threadName);
                if (result !=0){
                    System.out.printf("remove %s fail\n", threadName);
                }else {
                    System.out.printf("remove %s success\n", threadName);
                }
                consoleLock.unlock();
                threadRegistry.freeWriteLock();
                break;
            }
            consoleLock.lock();
            System.out.printf("working thread:%s\n", threadName);
            consoleLock.unlock();

            try{
                Thread.sleep(10);
            }catch (InterruptedException e){

                threadRegistry.getWriteLock();
                consoleLock.lock();
                int result = threadRegistry.remove(threadName);
                if (result !=0){
                    System.out.printf("remove %s fail\n", threadName);
                }else {
                    System.out.printf("remove %s success\n", threadName);
                }
                consoleLock.unlock();
                threadRegistry.freeWriteLock();
                break;

            }

        }


    }
}

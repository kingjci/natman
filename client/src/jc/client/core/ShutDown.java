package jc.client.core;

import jc.Version;

/**
 * Created by 金成 on 2015/9/25.
 */
public class ShutDown extends Thread {

    private ThreadRegistry threadRegistry;

    private int errorCode;

    public ShutDown(ThreadRegistry threadRegistry){

        this.threadRegistry = threadRegistry;

    }

    @Override
    public void run() {


        try{

            synchronized (this){
                this.wait();
            }


            //开始清理各 个线程
            int threadStillAlive = threadRegistry.shutDownAllThreads();

            if (Version.isDebug()){
                System.out.printf("ShutDown:%d threads is still alive when exit\n", threadStillAlive);
            }

            if (threadStillAlive !=0){
                System.out.println("ShutDown:shutDown fail!");
            }else {
                System.out.println("ShutDown:shutDown success!");
            }

            System.exit(errorCode);

        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }

    public void waitForShutDown(){

        //注册钩子,添加System.exit()之后的shutDownHook
        Runtime.getRuntime().addShutdownHook(new ShutDownHook());

        this.start();

    }


    public void shutDown(int errorCode){

        //设置退出错误代码
        this.errorCode = errorCode;

        System.out.println("ShutDown:prepare to shutDown...");

        //通知run函数继续运行，调用threadRegistry.shutDownAllThreads()关闭所有线程
        synchronized (this){
            this.notifyAll();
        }



    }

    private class ShutDownHook extends Thread{

        @Override
        public void run() {
            //这里可以添加一些扫尾工作，比如关闭服务器的连接，取消端口的占用等
            System.out.println("Goodbye!");
        }
    }
}

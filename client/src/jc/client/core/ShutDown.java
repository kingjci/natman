package jc.client.core;

import jc.Version;

/**
 * Created by ��� on 2015/9/25.
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


            //��ʼ����� ���߳�
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

        //ע�ṳ��,���System.exit()֮���shutDownHook
        Runtime.getRuntime().addShutdownHook(new ShutDownHook());

        this.start();

    }


    public void shutDown(int errorCode){

        //�����˳��������
        this.errorCode = errorCode;

        System.out.println("ShutDown:prepare to shutDown...");

        //֪ͨrun�����������У�����threadRegistry.shutDownAllThreads()�ر������߳�
        synchronized (this){
            this.notifyAll();
        }



    }

    private class ShutDownHook extends Thread{

        @Override
        public void run() {
            //����������һЩɨβ����������رշ����������ӣ�ȡ���˿ڵ�ռ�õ�
            System.out.println("Goodbye!");
        }
    }
}

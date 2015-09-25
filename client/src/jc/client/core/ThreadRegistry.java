package jc.client.core;

import jc.Version;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static jc.client.core.Main.consoleLock;
import static jc.Version.isDebug;

/**
 * Created by ��� on 2015/9/25.
 */
public class ThreadRegistry {

    private boolean shutDown = false;

    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock(false);

    private Map<String, Thread> threads = new HashMap<String, Thread>();

    public boolean register(String name,Thread thread){

        readWriteLock.writeLock().lock();
        if (threads.containsKey(name)){
            return false;
        }
        threads.put(name, thread);
        readWriteLock.writeLock().unlock();
        return true;
    }

    public int remove(String name){

        Thread thread = null;
        readWriteLock.writeLock().lock();
        thread = threads.remove(name);
        readWriteLock.writeLock().unlock();

        //�̴߳��ڲ�����ɾ������0���̲߳����ڷ���-1
        if (isDebug() && thread == null){
            consoleLock.lock();
            System.out.printf("remove thread %s failure\n", name);
            consoleLock.unlock();
        }

        return thread != null ? 0 : -1;
    }

    public int size(){
        int size = threads.size();
        return size;

    }

    public int shutDownAllThreads(){

        this.shutDown = true;

        readWriteLock.readLock().lock();
        for (Map.Entry<String, Thread> entry : threads.entrySet()) {
            entry.getValue().interrupt();
        }
        readWriteLock.readLock().unlock();

        consoleLock.lock();
        System.out.println("ThreadRegistry:interrupt all threads!");
        consoleLock.unlock();

        int count = 0;
        while (!threads.isEmpty()){

            if (++count > 10){
                //����10������������˳��Ļ���ǿ���˳�
                break;
            }

            try{
                consoleLock.lock();
                System.out.printf("ThreadRegistry:%d threads is still alive...\n", size());
                consoleLock.unlock();
                if (Version.isDebug()){
                    //��debugģʽ�£���ӡ�����е��߳�
                    showAllThread();
                }
                Thread.sleep(100);
            }catch (InterruptedException e){
                e.printStackTrace();
            }

        }

        if (!threads.isEmpty()){

            consoleLock.lock();
            System.out.printf("ThreadRegistry:%d threads is still alive after shutDownAllThreads\n", threads.size());
            consoleLock .unlock();
            showAllThread();

        }

        //����δ�ر��̵߳���Ŀ����Ϊ0��ʱ��ȫ����ȷ�ر�
        return size();


    }

    public void showAllThread(){

        readWriteLock.readLock().lock();
        consoleLock.lock();

        System.out.println("-----------------------------------------");
        if (threads.isEmpty()){
            System.out.println("ThreadRegistry:No thread is still alive");
        }else {
            System.out.printf("ThreadRegistry:%d threads is still alive", size());
        }
        for (Map.Entry<String, Thread> entry : threads.entrySet()){
            System.out.printf("thread:%s\n", entry.getKey());
        }
        System.out.println("-----------------------------------------");

        consoleLock.unlock();
        readWriteLock.readLock().unlock();

    }

    public boolean isShutDown() {
        return shutDown;
    }

    public void setShutDown(boolean shutDown) {
        this.shutDown = shutDown;
    }

    public void getReadLock(){
        this.readWriteLock.readLock().lock();
    }

    public void freeReadLock(){
        this.readWriteLock.readLock().unlock();
    }

    public void getWriteLock(){
        this.readWriteLock.writeLock().lock();
    }

    public void freeWriteLock(){
        this.readWriteLock.writeLock().unlock();
    }
}

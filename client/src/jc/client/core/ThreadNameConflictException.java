package jc.client.core;

/**
 * Created by ��� on 2015/9/25.
 */
public class ThreadNameConflictException extends Exception {

    private String threadName;

    public ThreadNameConflictException(String threadName){
        this.threadName = threadName;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

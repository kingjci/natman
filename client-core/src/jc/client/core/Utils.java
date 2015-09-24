package jc.client.core;

import jc.server.core.connection.Connection;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Created by 金成 on 2015/9/8.
 */
public class Utils {

    public static void Go(Runnable runnable){

        Thread thread = new Thread(runnable);
        thread.start();

    }

    public static void Join(Connection c1, Connection c2){

        //这个程序会等到两个Connection的双向传输都传送结束才结束返回
        CountDownLatch waitGroup = new CountDownLatch(2);
        Go(new Pipe(c1, c2, waitGroup));
        Go(new Pipe(c2, c1, waitGroup));
        System.out.printf("Joined %s with %s\n", c1.Id(), c2.Id());

        try{
            waitGroup.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }

    public static Connection Dial(String host, int port, String type){

        Socket socket = null;
        Connection connection = null;
        try{
            socket = new Socket(host, port);
            connection = new Connection(socket, type);
            System.out.printf("New connection to: %s\n", connection.getRemoteAddr());

        }catch (IOException e){
            e.printStackTrace();
        }
        return connection;

    }
}

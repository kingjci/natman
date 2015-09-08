package jc.server.core;

import jc.message.Message;
import jc.server.core.connection.Connection;
import jc.server.core.connection.Pipe;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Created by ½ð³É on 2015/9/8.
 */
public class Utils {

    public static void go(Runnable runnable){

        Thread thread = new Thread(runnable);
        thread.start();

    }

    public static void Join(Connection c1, Connection c2){

        CountDownLatch waitGroup = new CountDownLatch(2);
        go(new Pipe(c1, c2, waitGroup));
        go(new Pipe(c2, c1, waitGroup));
        System.out.printf("Joined %s with %s\n", c1.Id(), c2.Id());

        try{
            waitGroup.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }

    public static void WriteMessage(Connection connection, Message message){
        OutputStream outputStream = null;
        ObjectOutput objectOutput = null;

        try{
            outputStream  = connection.getSocket().getOutputStream();
            objectOutput = new ObjectOutputStream(outputStream);
            objectOutput.writeObject(message);
            objectOutput.flush();


        }catch (IOException e){
            e.printStackTrace();
        }

    }

}

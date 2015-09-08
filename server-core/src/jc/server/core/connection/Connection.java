package jc.server.core.connection;

import jc.server.core.Main;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Created by ½ð³É on 2015/9/8.
 */
public class Connection {

    private Socket socket;
    private int id;
    private String type;

    public Connection(Socket socket, String type){
        this.socket = socket;
        this.type = type;
        this.id = Main.random.getRandomInt();
    }

    public void Close(){
        try {
            this.socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public String Id(){
        return String.format("%s:%d", this.type, this.id);
    }

    public void SetType(String type){
        this.type = type;
    }

    public void CloseRead(){
        try{
            this.socket.shutdownInput();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public Socket getSocket(){
        return this.socket;
    }

    public static void Join(Connection c1, Connection c2){

        CountDownLatch waitGroup = new CountDownLatch(2);
        (new Thread(new Pipe(c1, c2, waitGroup))).start();
        (new Thread(new Pipe(c2, c1, waitGroup))).start();
        System.out.printf("Joined %s with %s\n", c1.Id(), c2.Id());

        try{
            waitGroup.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }


}

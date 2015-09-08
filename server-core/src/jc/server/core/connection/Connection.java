package jc.server.core.connection;

import jc.server.core.Main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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






}

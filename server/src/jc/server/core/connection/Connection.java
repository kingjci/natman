package jc.server.core.connection;

import jc.message.Message;

import java.io.*;
import java.net.Socket;

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
    }

    public void close(){
        try {
            this.socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public String Id(){
        return String.format("%s:%d", this.type, this.id);
    }

    public void setType(String type){
        this.type = type;
    }

    public void closeRead(){
        try{
            this.socket.shutdownInput();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public Socket getSocket(){
        return this.socket;
    }

    public void writeMessage(Message message){
        OutputStream outputStream = null;
        ObjectOutput objectOutput = null;

        try{

            outputStream  = this.socket.getOutputStream();
            objectOutput = new ObjectOutputStream(outputStream);
            objectOutput.writeObject(message);
            objectOutput.flush();

        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public Message readMessage(){
        InputStream inputStream = null;
        ObjectInput objectInput = null;
        Message message = null;
        try{
            inputStream = this.socket.getInputStream();
            objectInput = new ObjectInputStream(inputStream);
            message =(Message) objectInput.readObject();
        }catch (IOException e){
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }

        return message;
    }

    public String getRemoteAddr(){

        return this.socket.getRemoteSocketAddress().toString();

    }






}

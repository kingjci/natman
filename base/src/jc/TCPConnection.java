package jc;

import jc.message.Message;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ½ð³É on 2015/9/8.
 */
public class TCPConnection {

    private Socket socket;
    private String connectionId;
    private String type;
    private Lock outputLock;
    private Lock inputLock;

    public TCPConnection(Socket socket, String type, String connectionId){
        this.socket = socket;
        this.type = type;
        this.outputLock = new ReentrantLock(false);
        this.inputLock = new ReentrantLock(false);
        this.connectionId = connectionId;
    }

    public void close(){
        try {
            this.socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getType() {
        return type;
    }

    public String Id(){
        return String.format("%s:%s", this.type, this.connectionId);
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

    public void writeMessage(Message message) throws IOException{
        OutputStream outputStream = null;
        ObjectOutput objectOutput = null;

        try{

            outputLock.lock();
            outputStream  = socket.getOutputStream();
            objectOutput = new ObjectOutputStream(outputStream);
            objectOutput.writeObject(message);
            objectOutput.flush();


        }catch (IOException e){
            throw e;
        }finally {
            outputLock.unlock();
        }

    }

    public Message readMessage () throws IOException{
        InputStream inputStream = null;
        ObjectInput objectInput = null;
        Message message = null;
        try{
            inputLock.lock();
            inputStream = socket.getInputStream();
            objectInput = new ObjectInputStream(inputStream);
            message =(Message) objectInput.readObject();
        }catch (IOException e){
            throw e;
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }finally {
            inputLock.unlock();
        }

        return message;
    }

    public void write(byte[] payload) throws IOException{

        OutputStream outputStream = null;

        try{

            outputLock.lock();
            outputStream = socket.getOutputStream();
            outputStream.write(payload);
            outputStream.flush();


        }catch (IOException e){
            throw e;
        }finally {
            outputLock.unlock();
        }
    }

    public byte[] readAll() throws IOException{

        final int bufLength = 100;
        InputStream inputStream = null;
        List<byte[]> list = new LinkedList<byte[]>();
        byte[] result = null;

        byte[] buf = new byte[bufLength];

        try{
            inputLock.lock();
            inputStream = socket.getInputStream();
            while (inputStream.read(buf) != -1){
                list.add(buf);
                buf = new byte[bufLength];
            }

            int length = list.size()*bufLength;
            result = new byte[length];
            for (int i=0; i < list.size(); i++){
                System.arraycopy(list.get(i),0,result,i*bufLength, bufLength);
            }


        }catch (IOException e){
            throw e;
        }finally {
            inputLock.unlock();
        }

        return result;

    }

    public String getRemoteAddr(){

        return this.socket.getRemoteSocketAddress().toString();

    }






}

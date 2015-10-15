package jc;

import jc.message.Message;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TCPConnection {

    private final Socket socket;
    private final String connectionId;
    private InputStream inputStream;
    private OutputStream outputStream;
    private String type;
    private final Lock outputLock;
    private final Lock inputLock;

    private final Logger runtimeLogger;
    private final Logger accessLogger;

    public TCPConnection(
            Socket socket,
            String type,
            String connectionId,
            Logger runtimeLogger,
            Logger accessLogger
    ){
        this.socket = socket;
        this.type = type;
        this.connectionId = connectionId;
        this.runtimeLogger = runtimeLogger;
        this.accessLogger = accessLogger;

        outputLock = new ReentrantLock(false);
        inputLock = new ReentrantLock(false);
    }

    public void close(){
        try {
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
        }catch (IOException e){
            runtimeLogger.error(e.getMessage(), e);
        }
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public InputStream getInputStream() throws IOException{
        return this.socket.getInputStream();
    }

    public OutputStream getOutPutStream() throws IOException{
        return this.socket.getOutputStream();
    }

    public void shutdownInput() throws IOException{
        this.socket.shutdownInput();
    }

    public void shutdownOutput() throws IOException{
        this.socket.shutdownOutput();
    }

    public void writeMessage(Message message) throws IOException{

        try{

            outputLock.lock();
            OutputStream outputStream  = socket.getOutputStream();
            ObjectOutput objectOutput = new ObjectOutputStream(outputStream);
            objectOutput.writeObject(message);
            objectOutput.flush();

        }catch (IOException e){
            throw e;
        }finally {
            outputLock.unlock();
        }

    }

    public Message readMessage () throws IOException{

        Message message = null;
        try{
            inputLock.lock();
            InputStream inputStream = socket.getInputStream();
            ObjectInput objectInput = new ObjectInputStream(inputStream);
            message =(Message) objectInput.readObject();
        }catch (IOException e){
            throw e;
        }catch (ClassNotFoundException e){
            runtimeLogger.error(e.getMessage(), e);
        }finally {
            inputLock.unlock();
        }

        return message;
    }

    public String getRemoteAddress(){
        return this.socket.getRemoteSocketAddress().toString();
    }






}

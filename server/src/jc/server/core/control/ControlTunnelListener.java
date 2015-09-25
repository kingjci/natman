package jc.server.core.control;

import jc.server.core.connection.Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Created by ½ð³É on 2015/9/23.
 */
public class ControlTunnelListener implements Runnable{

    private BlockingQueue<Connection> listener;
    private ServerSocket serverSocket;

    ControlTunnelListener(BlockingQueue<Connection> listener, int port){

        this.listener = listener;
        try{
            serverSocket = new ServerSocket(port);
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        while (true){


            try{
                Socket socket = serverSocket.accept();
                Connection connection = new Connection(socket, "control/proxy");
                System.out.printf("New connection from %s\n",connection.getRemoteAddr());
                listener.put(connection);
            }catch (IOException e){
                e.printStackTrace();
            }catch(InterruptedException e){
                e.printStackTrace();
            }



        }
    }
}




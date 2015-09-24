package jc.server.core.control;

import jc.server.core.connection.Connection;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static jc.server.core.Utils.Go;

/**
 * Created by ½ð³É on 2015/9/23.
 */
public class ControlTunnel implements Runnable{

    protected BlockingQueue<Connection> listener = new LinkedBlockingQueue<Connection>();
    protected int port;



    public ControlTunnel(int port){
        this.port = port;
    }

    @Override
    public void run() {

        Go(new ControlTunnelListener(listener, port));
        System.out.printf("Listening for control and proxy connections on %d\n", port);

        while (true){

            try{
                Connection connection = listener.take();
                Go(new ControlTunnelHandler(connection));
            }catch (InterruptedException e){
                e.printStackTrace();
            }





        }


    }
}

package jc.server.core.ControlTunnel;

import jc.Connection;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static jc.server.core.Utils.Go;
import static jc.server.core.Utils.timeStamp;

/**
 * Created by ��� on 2015/9/23.
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
        System.out.printf("[%s][ControlTunnel]Listening for control and proxy connections on %d\n", timeStamp(),port);

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

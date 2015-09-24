package jc.server.core.control;

import jc.message.Message;
import jc.server.core.connection.Connection;

import java.net.SocketException;

/**
 * Created by ½ð³É on 2015/9/23.
 */
public class ControlTunnelHandler implements Runnable {

    protected Connection connection;

    ControlTunnelHandler(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {

        try {
            if (connection == null){
                System.out.println("connection is null:ControlTunnelHandler->run");
                return;
            }

            connection.getSocket().setSoTimeout(10 * 1000);
            Message message = connection.readMessage();
            switch (message.getMessageType()) {
                case "AuthRequest":


                    break;
                case "ProxyResponse":


                    break;

                default:
                    System.out.println("unknown message:ControlTunnel->run");

            }


        } catch (SocketException e) {
            e.printStackTrace();
        }finally {

            if (connection != null){
                connection.close();
            }
        }


    }
}
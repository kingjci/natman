package jc.server.core.ControlTunnel;

import jc.message.AuthRequest;
import jc.message.Message;
import jc.Connection;
import jc.message.ProxyResponse;
import jc.server.core.ControlConnection;

import java.io.IOException;

import static jc.server.core.Main.controlConnectionRegistry;
import static jc.server.core.Utils.Go;
import static jc.server.core.Utils.timeStamp;

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


            if (connection == null){
                System.out.printf("[%s][ControlTunnelHandler]connection is null\n", timeStamp());
                return;
            }

            //connection.getSocket().setSoTimeout(10 * 1000);
            Message message = null;
            try {
                message = connection.readMessage();
            }catch (IOException e){
                e.printStackTrace();
            }

            if (message == null){
                System.out.printf("[%s][ControlTunnelHandler]message is null\n", timeStamp());
                return;
            }

            switch (message.getMessageType()) {
                case "AuthRequest":


                    ControlConnection controlConnection = new ControlConnection(connection, (AuthRequest)message);

                    Go(controlConnection);


                    break;
                case "ProxyResponse":{

                    ProxyResponse proxyResponse = (ProxyResponse) message;

                    connection.setType("proxy");

                    System.out.printf("[%s][ControlTunnelHandler]Registering new proxy connection[%s] for %s[%s]\n", timeStamp(),connection.getConnectionId(),proxyResponse.getIP(),proxyResponse.getClientId());

                    ControlConnection storedControlConnection = controlConnectionRegistry.get(proxyResponse.getClientId());

                    if (storedControlConnection == null){
                        System.out.printf("[%s][ControlTunnelHandler]No client found for identifier:%s\n" , timeStamp(),proxyResponse.getClientId());
                        break;
                    }

                    storedControlConnection.registerProxy(connection);
                    break;

                }

                default:
                    System.out.printf("[%s][ControlTunnelHandler]unknown message\n", timeStamp());

            }


    }
}
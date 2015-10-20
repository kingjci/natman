package jc;


import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.CountDownLatch;

public class Utils {

    public static void Go(Runnable runnable){

        Thread thread = new Thread(runnable);
        thread.start();

    }

    public static void Join(
            TCPConnection c1,
            TCPConnection c2,
            Logger runtimeLogger,
            Logger accessLogger){

        CountDownLatch waitGroup = new CountDownLatch(2);
        Go(new Pipe(c1, c2, waitGroup, runtimeLogger));
        Go(new Pipe(c2, c1, waitGroup, runtimeLogger));

        runtimeLogger.debug(
                String.format(
                        "Join %s[%s] with %s[%s]",
                        c1.getRemoteAddress(),
                        c1.getConnectionId(),
                        c2.getRemoteAddress(),
                        c2.getConnectionId()
                )
        );

        try{
            waitGroup.await();
            c1.close();
            c2.close();
        }catch (IOException|InterruptedException e){
            runtimeLogger.error(e.getMessage(),e);
        }

       runtimeLogger.debug(
                String.format(
                        "Detach %s[%s] with %s[%s]",
                        c1.getRemoteAddress(),
                        c1.getConnectionId(),
                        c2.getRemoteAddress(),
                        c2.getConnectionId()
                )
        );
    }

    public static TCPConnection Dial(
            String host,
            int port,
            String type,
            String connectionId,
            Logger runtimeLogger,
            Logger accessLogger
    ){

        TCPConnection tcpConnection = null;
        try{
            Socket socket = new Socket();
            SocketAddress address = new InetSocketAddress(host, port);
            socket.connect(address, 3*1000);
            tcpConnection = new TCPConnection(socket, type, connectionId, runtimeLogger, accessLogger);
            accessLogger.debug(
                    String.format("New %s connection[%s] to: %s",
                            tcpConnection.getType(),
                            tcpConnection.getConnectionId(),
                            tcpConnection.getRemoteAddress())
            );
        }catch (IOException e){

            runtimeLogger.error(
                    String.format("Can not connect to %s:%d, perhaps this port is not open",
                            host,
                            port)
            );
            //runtimeLogger.debug(e.getMessage(),e);

        }
        return tcpConnection;

    }


}

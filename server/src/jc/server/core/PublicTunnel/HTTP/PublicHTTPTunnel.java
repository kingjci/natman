package jc.server.core.PublicTunnel.HTTP;


import jc.Random;
import jc.TCPConnection;
import jc.server.core.ControlConnection.ControlConnection;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PublicHTTPTunnel extends Thread{

    private int port;
    private Map<String, ControlConnection> urls;
    private Map<String, List<String>> clientIdToUrls;
    private ReadWriteLock urlsLock;
    private ServerSocket serverSocket;
    private Random random;
    private final Logger runtimeLogger;
    private final Logger accessLogger;

    public PublicHTTPTunnel(
            int port,
            Random random,
            Logger runtimeLogger,
            Logger accessLogger
    ){
        urls = new HashMap<>();
        urlsLock = new ReentrantReadWriteLock(false);

        this.port = port;
        this.random = random;
        this.runtimeLogger = runtimeLogger;
        this.accessLogger = accessLogger;
    }



    public String bind(int port){

        this.port = port;

        try{

            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("0.0.0.0", port));

            accessLogger.info(
                    String.format("Initiate HTTP tunnel %d successfully", port)
            );

            return "success";

        }catch (BindException e){


            String result =
                    String.format(
                            "Bind HTTP tunnel %d failure:" +
                                    "the port is already used by other software",
                            port
                    );

            accessLogger.info(result);
            return "failure";

        }catch (IOException e){
            String result = String.format("Unknown exception occurs when bind HTTP tunnel %s", port);
            accessLogger.info(result);
            return result;
        }

    }

    public String register(String publicUrl, ControlConnection controlConnection){

        urlsLock.writeLock().lock();

        if (urls.containsKey(publicUrl)){

            urlsLock.writeLock().unlock();
            String result = String.format(
                    "Conflict subdomain %s",
                    publicUrl
            );
            runtimeLogger.warn(result);
            return result;
        }

        urls.put(publicUrl, controlConnection);
        urlsLock.writeLock().unlock();

        runtimeLogger.info(
                String.format(
                        "Success to add HTTP Tunnel from %s[%s] on port %d",
                        controlConnection.getRemoteAddress(),
                        controlConnection.getClientId(),
                        port
                )
        );

        return "success";
    }

    public String delete(String clientId){

        urlsLock.writeLock().lock();

        List<String> urlsOfClientId = clientIdToUrls.get(clientId);

        for (String url : urlsOfClientId){
            urls.remove(url);
        }
        urlsLock.writeLock().unlock();

        return "success";
    }


    @Override
    public void run() {


        while(true) {

            try {

                Socket socket = serverSocket.accept();
                TCPConnection httpConnection =
                        new TCPConnection(
                                socket,
                                "public",
                                random.getRandomConnectionId(),
                                runtimeLogger,
                                accessLogger
                        );

                accessLogger.debug(
                        String.format("New public HTTP Connection[%s] from %s",
                                httpConnection.getConnectionId(),
                                httpConnection.getRemoteAddress())
                );


            } catch (IOException e) {
                //   if public connection is closed by another side, this exception
                //will occurs, program will runs to here. But indeed is not a
                //exception. Just do nothing
            }
        }
    }


    public void close() {

        //This method is called only when the last
        // http tunnel of the port is deleted

        try{
            serverSocket.close();
        }catch (IOException e){
            runtimeLogger.error(e.getMessage(),e);
        }



    }
}

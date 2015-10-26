package jc.server.core.PublicTunnel.TCP;

import jc.Random;
import jc.TCPConnection;
import jc.message.PublicTunnelRequest;
import jc.server.core.Config;
import jc.server.core.ControlConnection.ControlConnection;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class PublicTCPTunnel extends Thread{

    private String publicUrl;
    private ServerSocket serverSocket;
    private ControlConnection controlConnection;
    private Random random;
    private Config config;
    private final Logger runtimeLogger;
    private final Logger accessLogger;

    public PublicTCPTunnel(
            ControlConnection controlConnection,
            Config config,
            Random random,
            Logger runtimeLogger,
            Logger accessLogger
    ){
        this.controlConnection = controlConnection;
        this.config = config;
        this.random = random;
        this.runtimeLogger = runtimeLogger;
        this.accessLogger = accessLogger;
    }

    public int getPort(){
        return serverSocket.getLocalPort();
    }

    public String bind(int port){

        publicUrl = String.format("tcp://%s:%d",config.getDomain(),port);

        try{

            this.serverSocket = new ServerSocket();
            this.serverSocket.bind(new InetSocketAddress("0.0.0.0", port));
            accessLogger.info(
                    String.format("Bind public TCP tunnel %s successfully", publicUrl)
            );

            return "success";
        }catch (BindException e) {

            try{
                serverSocket.bind(new InetSocketAddress("0.0.0.0", 0));
            }catch (IOException ee){
                runtimeLogger.error(e.getMessage(), e);
            }

            String result =
                    String.format(
                            "Bind public tunnel %s failure:" +
                                    "the port is already used by other software, choose a random port %d",
                            publicUrl,
                            serverSocket.getLocalPort()
                    );

            accessLogger.info(result);
            return "success";
        }catch (IOException e){
            String result = String.format("Unknown exception occurs when bind %s", publicUrl);
            accessLogger.info(result);
            return result;
        }

    }

    @Override
    public void run() {

        while (true){
            try{

                Socket socket = serverSocket.accept();
                TCPConnection tcpConnection =
                        new TCPConnection(
                                socket,
                                "public",
                                random.getRandomString(8),
                                runtimeLogger,
                                accessLogger
                        );

                accessLogger.debug(
                        String.format("New public Connection[%s] from %s",
                                tcpConnection.getConnectionId(),
                                tcpConnection.getRemoteAddress())
                );

                (new PublicTCPTunnelHandler(controlConnection, tcpConnection, publicUrl)).start();

            }catch (IOException e){
                //   if public connection is closed by another side, this exception
                //will occurs, program will runs to here. But indeed is not a
                //exception. Just do nothing
            }


        }

    }

    public void close(){

        try{
            serverSocket.close();
        }catch (IOException e){
            runtimeLogger.error(e.getMessage(), e);
        }

    }
}

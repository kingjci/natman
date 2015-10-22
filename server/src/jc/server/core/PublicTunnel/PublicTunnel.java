package jc.server.core.PublicTunnel;

import jc.Random;
import jc.TCPConnection;
import jc.message.PublicTunnelRequest;
import jc.server.core.ControlConnection.ControlConnection;
import org.apache.log4j.Logger;

import static jc.Utils.*;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class PublicTunnel implements Runnable{

    protected PublicTunnelRequest publicTunnelRequest;
    protected int port;
    protected String publicUrl;
    protected String protocol;
    protected ServerSocket serverSocket;
    protected ControlConnection controlConnection;
    private Random random;
    private final Logger runtimeLogger;
    private final Logger accessLogger;

    public PublicTunnel(
            PublicTunnelRequest publicTunnelRequest,
            String publicUrl,
            ControlConnection controlConnection,
            Random random,
            Logger runtimeLogger,
            Logger accessLogger
            ){


        this.publicTunnelRequest = publicTunnelRequest;
        this.publicUrl = publicUrl;
        this.controlConnection = controlConnection;
        this.random = random;
        this.runtimeLogger = runtimeLogger;
        this.accessLogger = accessLogger;
        this.protocol = publicTunnelRequest.getProtocol();


    }

    public int getPort() {
        return port;
    }

    public String bind(int port){

        this.port = port;

        switch (protocol){

            case "tcp":

                try{

                    this.serverSocket = new ServerSocket();
                    this.serverSocket.bind(new InetSocketAddress("0.0.0.0", port));
                    accessLogger.info(
                            String.format("Bind public tunnel %s successfully", publicUrl)
                    );

                    return "success";
                }catch (BindException e) {

                    try{
                        this.serverSocket.bind(new InetSocketAddress("0.0.0.0", 0));
                        this.port = this.serverSocket.getLocalPort();
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

            case "http":

                return "success";

            case "udp":

                return "success";

            default:
                return null;

        }
    }

    @Override
    public void run() {


        switch (protocol){

            case "tcp":

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
                        Go(new PublicTunnelTCPHandler(controlConnection, tcpConnection, publicUrl));

                    }catch (IOException e){
                        //   if public connection is closed by another side, this exception
                        //will occurs, program will runs to here. But indeed is not a
                        //exception. Just do nothing
                    }
                }


            case "http":

                break;


            case "udp":

                break;

        }

    }

    public void close(){

        try{
            serverSocket.close();
        }catch (IOException e){
            runtimeLogger.error(e.getMessage(), e);
        }

    }

    public String getPublicUrl() {
        return publicUrl;
    }
}

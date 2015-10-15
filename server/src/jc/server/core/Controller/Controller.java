package jc.server.core.Controller;

import jc.Random;
import jc.TCPConnection;
import jc.command.Command;
import jc.server.core.Config;
import jc.server.core.ControlConnection.ControlConnectionRegistry;
import jc.server.core.Option;
import jc.server.core.PublicTunnel.PublicTunnelRegistry;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import static jc.Utils.Go;

public class Controller implements Runnable{

    private int port;
    private PublicTunnelRegistry publicTunnelRegistry;
    private ControlConnectionRegistry controlConnectionRegistry;
    private BlockingQueue<Command> commands;
    private Random random;
    private Config config;
    private Option option;
    private Logger runtimeLogger;
    private Logger accessLogger;

    //ControlTunnel use tcp protocol
    private ServerSocket serverSocket;

    public Controller(
            int port,
            PublicTunnelRegistry publicTunnelRegistry,
            ControlConnectionRegistry controlConnectionRegistry,
            BlockingQueue<Command> commands,
            Random random,
            Config config,
            Option option,
            Logger runtimeLogger,
            Logger accessLogger
    ){

        this.port = port;
        // pass ControlTunnelHandler to register control connection
        this.publicTunnelRegistry = publicTunnelRegistry;
        this.controlConnectionRegistry = controlConnectionRegistry;
        this.commands = commands;
        this.random = random;
        this.config = config;
        this.option = option;
        this.runtimeLogger = runtimeLogger;
        this.accessLogger = accessLogger;

    }

    public PublicTunnelRegistry getPublicTunnelRegistry() {
        return publicTunnelRegistry;
    }

    public ControlConnectionRegistry getControlConnectionRegistry() {
        return controlConnectionRegistry;
    }

    public BlockingQueue<Command> getCommands() {
        return commands;
    }

    public Random getRandom() {
        return random;
    }

    public Config getConfig() {
        return config;
    }

    public Option getOption() {
        return option;
    }

    public Logger getRuntimeLogger() {
        return runtimeLogger;
    }

    public Logger getAccessLogger() {
        return accessLogger;
    }

    @Override
    public void run() {

        try{

            serverSocket = new ServerSocket(port);
            runtimeLogger.info(
                    String.format("NatMan server start on %d", port));

        }catch (IOException e){
            runtimeLogger.error(e.getMessage(),e);
        }




        while (true){

            try{

                Socket socket = serverSocket.accept();
                //control tunnel get a tcp socket，wrap it into TCPConnection
                TCPConnection tcpConnection =
                        new TCPConnection(socket,
                                "control/proxy",
                                random.getRandomString(8),
                                runtimeLogger,
                                accessLogger
                        );

                accessLogger.info(
                        String.format("New control/proxy connection[%s] from %s",
                                tcpConnection.getConnectionId(),
                                tcpConnection.getRemoteAddress()));

                Go(new ControllerHandler(tcpConnection, this));

            }catch (IOException e){
                runtimeLogger.error(e.getMessage(),e);
            }
        }


    }
}

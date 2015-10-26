package jc.server.core.Controller;

import jc.Random;
import jc.TCPConnection;
import jc.command.Command;
import jc.server.core.Config;
import jc.server.core.ControlConnection.ControlConnectionRegistry;
import jc.server.core.Option;
import jc.server.core.PublicTunnel.TCP.PublicTCPTunnelRegistry;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class Controller extends Thread{

    private int port;
    private PublicTCPTunnelRegistry publicTCPTunnelRegistry;
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
            PublicTCPTunnelRegistry publicTCPTunnelRegistry,
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
        this.publicTCPTunnelRegistry = publicTCPTunnelRegistry;
        this.controlConnectionRegistry = controlConnectionRegistry;
        this.commands = commands;
        this.random = random;
        this.config = config;
        this.option = option;
        this.runtimeLogger = runtimeLogger;
        this.accessLogger = accessLogger;

    }

    public PublicTCPTunnelRegistry getPublicTCPTunnelRegistry() {
        return publicTCPTunnelRegistry;
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

        }catch(BindException e) {
            runtimeLogger.error(String.format("Port %s is already in use", config.getControlPort()));
            System.exit(-1);
        }
        catch (IOException e){
            runtimeLogger.error(e.getMessage(),e);
            System.exit(-1);
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

                accessLogger.debug(
                        String.format("New connection[%s] from %s",
                                tcpConnection.getConnectionId(),
                                tcpConnection.getRemoteAddress()));

                (new ControllerHandler(tcpConnection, this)).start();

            }catch (IOException e){
                runtimeLogger.error(e.getMessage(),e);
            }
        }


    }
}

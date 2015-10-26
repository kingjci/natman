package jc.server.core;

import jc.Random;
import jc.command.Command;
import jc.command.QuitCommand;
import jc.server.core.ControlConnection.ControlConnectionRegistry;
import jc.server.core.Controller.Controller;
import jc.server.core.PublicTunnel.TCP.PublicTCPTunnelRegistry;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    public static Config CONFIG; //config loaded from cofig file
    public static Option OPTION; //store constant things
    public static Random RANDOM;
    public static Logger RUNTIMELOGGER;
    public static Logger ACCESSLOGGER;
    public static PublicTCPTunnelRegistry PUBLICTUNNELREGISTRY;
    public static ControlConnectionRegistry CONTROLCONNECTIONREGISTRY;
    public static BlockingQueue<Command> COMMANDS;
    public static Thread CONTROLLER;

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new ExitHandler());

        OPTION = new Option();
        RANDOM = new Random();
        RUNTIMELOGGER = Logger.getLogger("Runtime");
        ACCESSLOGGER = Logger.getLogger("Access");
        CONFIG = new Config(args,RUNTIMELOGGER);
        COMMANDS = new LinkedBlockingQueue<>();
        CONTROLCONNECTIONREGISTRY = new ControlConnectionRegistry(RUNTIMELOGGER, ACCESSLOGGER);
        PUBLICTUNNELREGISTRY = new PublicTCPTunnelRegistry(RUNTIMELOGGER,ACCESSLOGGER,CONFIG,RANDOM);

        CONTROLLER =  new Controller(
                CONFIG.getControlPort(),
                PUBLICTUNNELREGISTRY,
                CONTROLCONNECTIONREGISTRY,
                COMMANDS,
                RANDOM,
                CONFIG,
                OPTION,
                RUNTIMELOGGER,
                ACCESSLOGGER
        );

        CONTROLLER.start();

        //main thread receive commands from other threads to handle
        while (true){
            try{
                Command command = COMMANDS.take();
                switch (command.getCommandType()){

                    case "QuitCommand":

                        QuitCommand quitCommand = (QuitCommand) command;
                        System.exit(quitCommand.getExitCode());

                    default:
                        RUNTIMELOGGER.error("Unknown command");
                }
            }catch (InterruptedException e){
                RUNTIMELOGGER.error(e.getMessage(), e);
            }

        }

    }

}

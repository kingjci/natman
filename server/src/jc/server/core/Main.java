package jc.server.core;

import jc.Random;
import jc.command.Command;
import jc.command.QuitCommand;
import jc.server.core.ControlConnection.ControlConnectionRegistry;
import jc.server.core.Controller.Controller;
import jc.server.core.PublicTunnel.PublicTunnelRegistry;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static jc.Utils.Go;

public class Main {

    public static Config config = new Config(); //config loaded from cofig file
    public static Option option = new Option(); //store constant things
    public static Logger runtimeLogger = Logger.getLogger("Runtime");
    public static Logger accessLogger = Logger.getLogger("Access");
    public static Random random = new Random();
    public static PublicTunnelRegistry publicTunnelRegistry = new PublicTunnelRegistry(runtimeLogger,accessLogger,config);
    public static ControlConnectionRegistry controlConnectionRegistry = new ControlConnectionRegistry(runtimeLogger,accessLogger);
    public static BlockingQueue<Command> commands = new LinkedBlockingQueue<>();


    public static Controller controller =
            new Controller(option.getControlPort(),
                    publicTunnelRegistry,
                    controlConnectionRegistry,
                    commands,
                    random,
                    config,
                    option,
                    runtimeLogger,
                    accessLogger);

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new ExitHandler());

        Go(controller);

        //main thread receive commands from other threads to handle
        while (true){
            try{
                Command command = commands.take();
                switch (command.getCommandType()){

                    case "QuitCommand":

                        QuitCommand quitCommand = (QuitCommand) command;

                        System.exit(quitCommand.getExitCode());

                    default:
                        runtimeLogger.error("Unknown command");
                }
            }catch (InterruptedException e){
                runtimeLogger.error(e.getMessage(), e);
            }

        }

    }

}

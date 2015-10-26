package jc.client.core;

import jc.Random;
import jc.command.Command;
import jc.command.QuitCommand;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    public static Option OPTION;
    public static Random RANDOM;
    public static Config CONFIG;
    public static Logger RUNTIMELOGGER;
    public static Logger ACCESSLOGGER;
    public static BlockingQueue<Command> COMMANDS;
    public static Thread CONTROLLER;


    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new ExitHandler());

        OPTION = new Option();
        RANDOM = new Random();
        RUNTIMELOGGER = Logger.getLogger("Runtime");
        ACCESSLOGGER = Logger.getLogger("Access");
        CONFIG = new Config(args, OPTION, RUNTIMELOGGER);
        COMMANDS = new LinkedBlockingQueue<>();

        CONTROLLER = new Controller(
                CONFIG,
                OPTION,
                RANDOM,
                COMMANDS,
                RUNTIMELOGGER,
                ACCESSLOGGER
        );

        CONTROLLER.start();

        while (true){
            try{
                Command command = COMMANDS.take();
                switch (command.getCommandType()){

                    case "QuitCommand":

                        QuitCommand quitCommand = (QuitCommand) command;
                        RUNTIMELOGGER.info(String.format("QuitCommand, Reason:%s", quitCommand.getReason()));
                        //shutdown
                        System.exit(quitCommand.getExitCode());

                    default:
                        RUNTIMELOGGER.error("Unknown command");
                }
            }catch (InterruptedException e){
                RUNTIMELOGGER.error(e.getMessage(),e);
            }

        }


    }


}

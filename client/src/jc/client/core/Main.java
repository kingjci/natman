package jc.client.core;

import jc.Random;
import jc.command.Command;
import jc.command.QuitCommand;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static jc.Utils.Go;
import static jc.client.core.Config.LoadConfiguration;


public class Main {

    public final static Option OPTION = new Option();
    public final static Random RANDOM = new Random();
    public final static Config CONFIG = new Config();
    public final static Logger RUNTIMELOGGER = Logger.getLogger("Runtime");
    public final static Logger ACCESSLOGGER = Logger.getLogger("Access");
    public final static BlockingQueue<Command> COMMANDS = new LinkedBlockingQueue<>();
    public final static Controller CONTROLLER = new Controller(CONFIG, OPTION,RANDOM, COMMANDS,RUNTIMELOGGER, ACCESSLOGGER );


    public static void main(String[] args) {
        
        LoadConfiguration(args, CONFIG ,OPTION,RUNTIMELOGGER);

        Go(CONTROLLER);

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

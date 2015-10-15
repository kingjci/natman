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


public class Main {

    public final static Option OPTION = new Option();
    public final static Random RANDOM = new Random();
    public final static Config CONFIG = new Config();
    public final static Logger RUNTIMELOGGER = Logger.getLogger("Runtime");
    public final static Logger ACCESSLOGGER = Logger.getLogger("Access");
    public final static BlockingQueue<Command> COMMANDS = new LinkedBlockingQueue<>();
    public final static Controller CONTROLLER = new Controller(CONFIG, RANDOM, COMMANDS,RUNTIMELOGGER, ACCESSLOGGER );


    public static void main(String[] args) {
        
        LoadConfiguration(args);

        Go(CONTROLLER);

        while (true){
            try{
                Command command = COMMANDS.take();
                switch (command.getCommandType()){

                    case "QuitCommand":

                        QuitCommand quitCommand = (QuitCommand) command;
                        RUNTIMELOGGER.debug(String.format("QuitCommand, Reason:%s", quitCommand.getReason()));
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

    public static void LoadConfiguration(String[] args){

        Options options = new Options();
        options.addOption("config", true,"config path");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try{
            cmd = parser.parse(options, args);
        }catch (ParseException e){
            RUNTIMELOGGER.error(e.getMessage(),e);
        }

        if (cmd == null){
            RUNTIMELOGGER.error("command is null");
            return;
        }

        if (cmd.hasOption("config")){

            File config = null;
            FileReader fileReader = null;
            BufferedReader bufferedReader = null;
            try{
                fileReader = new FileReader(cmd.getOptionValue("config"));
                bufferedReader = new BufferedReader(fileReader);

                String line;
                int count = 0;
                while ( (line = bufferedReader.readLine()) != null){
                    count++;
                    if (line.startsWith("#")){
                        continue;
                    }


                    switch (line){

                        case "auth":

                            while ( "[/auth]".equals((line = bufferedReader.readLine()))){
                                count++;

                                if (line.startsWith("#")){
                                    continue;
                                }
                                String[] words = line.split(":");
                                CONFIG.setUsername(words[0]);
                                CONFIG.setPassword(words[1]);

                            }
                            break;

                        case "server":
                            while ( "[/server]".equals((line = bufferedReader.readLine()))){
                                count++;

                                if (line.startsWith("#")){
                                    continue;
                                }
                                CONFIG.setServerAddress(line);

                            }

                            if (CONFIG.getServerAddress() ==null | "".equals(CONFIG.getServerAddress())){
                                RUNTIMELOGGER.error("Please give a server address in the config");
                            }

                            break;

                        case "tcp":

                            while ( "[/tcp]".equals((line = bufferedReader.readLine()))){
                                count++;

                                if (line.startsWith("#")){
                                    continue;
                                }
                                String[] words = line.split(":");
                                PublicTunnelConfiguration publicTunnelConfiguration = new PublicTunnelConfiguration();
                                publicTunnelConfiguration.setProtocol("tcp");
                                publicTunnelConfiguration.setName(words[0]);
                                publicTunnelConfiguration.setLocalPort(Integer.valueOf(words[1]));
                                publicTunnelConfiguration.setRemotePort(Integer.valueOf(words[2]));

                                CONFIG.putPublicTunnelConfiguration(publicTunnelConfiguration);

                            }

                        case "http":


                            while ( "[/http]".equals((line = bufferedReader.readLine()))){
                                count++;

                                if (line.startsWith("#")){
                                    continue;
                                }
                                String[] words = line.split(":");
                                PublicTunnelConfiguration publicTunnelConfiguration = new PublicTunnelConfiguration();
                                publicTunnelConfiguration.setProtocol("http");
                                publicTunnelConfiguration.setName(words[0]);
                                publicTunnelConfiguration.setSubDomain(words[1]);
                                publicTunnelConfiguration.setLocalPort(Integer.valueOf(words[2]));
                                publicTunnelConfiguration.setRemotePort(Integer.valueOf(words[3]));

                                CONFIG.putPublicTunnelConfiguration(publicTunnelConfiguration);

                            }

                        case "udp":

                            while ( "[/udp]".equals((line = bufferedReader.readLine()))){
                                count++;

                                if (line.startsWith("#")){
                                    continue;
                                }
                                String[] words = line.split(":");
                                PublicTunnelConfiguration publicTunnelConfiguration = new PublicTunnelConfiguration();
                                publicTunnelConfiguration.setProtocol("udp");
                                publicTunnelConfiguration.setName(words[0]);
                                publicTunnelConfiguration.setLocalPort(Integer.valueOf(words[1]));
                                publicTunnelConfiguration.setRemotePort(Integer.valueOf(words[2]));

                                CONFIG.putPublicTunnelConfiguration(publicTunnelConfiguration);

                            }

                            break;

                        case "":

                            //  blank line
                            break;

                        default:

                            RUNTIMELOGGER.error("Unknown syntax");
                    }
                }

                RUNTIMELOGGER.info(String.format("Read %d config lines", count));

            }catch (FileNotFoundException e){
                RUNTIMELOGGER.error(e.getMessage(),e);
            }catch (IOException e){
                RUNTIMELOGGER.error(e.getMessage(),e);
            }finally {
                try{
                    bufferedReader.close();
                    fileReader.close();
                }catch (IOException e){
                    RUNTIMELOGGER.error("Error occurs when  closing the config file");
                }


            }

        }else {
            PublicTunnelConfiguration publicTunnelConfiguration = new PublicTunnelConfiguration();

            publicTunnelConfiguration.setName("default");


            CONFIG.putPublicTunnelConfiguration(publicTunnelConfiguration);


        }

        switch (args[0]){

            case "list":


                return;

            case "version":
                RUNTIMELOGGER.info(String.format("Natman version:%f",OPTION.getVersion()));
                return;


            case "help":
                RUNTIMELOGGER.info("Examples:\n" +
                        "\tnatman 80\n" +
                        "\tnatman -subdomain example\n" +
                        "\tnatman -hostname=\"example.com\"" +
                        "\n" +
                        "\n" +
                        "Advanced usage: natman [OPTIONS] <command> [command args] [...]\n" +
                        "Commands:\n" +
                        "\tnatman start [tunnel] [...]    Start tunnels by name from config file\n" +
                        "\tnatman list                    List tunnel names from config file\n" +
                        "\tnatman help                    Print help\n" +
                        "\tnatman version                 Print natman version\n" +
                        "\n" +
                        "Examples:\n" +
                        "\tnatman start www api blog pubsub\n" +
                        "\tnatman -log=stdout -config=natman.cfg start ssh\n" +
                        "\tnatman version\n" +
                        "\n" +
                        "`");

                return;

            case "":

                RUNTIMELOGGER.error("Error: Specify a local port to tunnel to, or " +
                        "an natman command.\n\nExample: To expose port 80, run " +
                        "'natman 80'");
                return;

            default:

                //show help
                RUNTIMELOGGER.error("Unknown command");
        }


    }
}

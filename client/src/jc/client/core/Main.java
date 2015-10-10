package jc.client.core;

import jc.Random;
import jc.Version;
import jc.client.core.command.Command;
import jc.client.core.command.QuitCommand;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static jc.Utils.Go;
import static jc.Utils.timeStamp;

/**
 * Created by 金成 on 2015/9/5.
 */
public class Main {


    public static Random random = new Random();

    public static Config config = new Config();

    private static BlockingQueue<Command> cmds = new LinkedBlockingQueue<>();

    private static ControlConnection controlConnection= new ControlConnection(config, random, cmds);

    public static void main(String[] args) {



        //解析参数
        LoadConfiguration(args);

        Go(controlConnection);

        while (true){
            try{
                Command command = cmds.take();
                switch (command.getCommandType()){

                    case "QuitCommand":

                        QuitCommand quitCommand = (QuitCommand) command;
                        controlConnection.shutDown(quitCommand.getReason());
                        System.out.printf("[%s][Controller]QuitCommand\n", timeStamp());
                        //shutdown
                        System.exit(-2);

                    default:
                        System.out.printf("[%s][Controller]unknown command\n", timeStamp());

                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }

        }


    }

    public static void LoadConfiguration(String[] args){

        switch (args[0]){

            case "list":

                //显示配置文件中的tunnels
                break;

            case "version":
                System.out.println("natman version:"+ Version.Current);
                return;


            case "help":
                System.out.println("Examples:\n" +
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

                System.out.println("Error: Specify a local port to tunnel to, or " +
                        "an natman command.\n\nExample: To expose port 80, run " +
                        "'natman 80'");
                return;

            default:
                //没有指定命令
                config.setCommand("default");
        }



        Options options = new Options();
        options.addOption("subdomain", true, "-subdomain kingjci");
        options.addOption("localport", true, "-localport 8080");
        options.addOption("remoteport", true, "-remoteport 8000");
        options.addOption("config", true, "-config config.cfg");
        options.addOption("hostname", true, "-hostname server.jincheng.link");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try{
            cmd = parser.parse(options, args);
        }catch (ParseException e){
            e.printStackTrace();
        }

        if (cmd == null){
            System.out.println("command is null");
            return;
        }

        if (cmd.hasOption("config")){
            File config = new File(cmd.getOptionValue("config"));
            FileReader fileReader   = null;
            try{
                fileReader = new FileReader(config);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }

            if (fileReader == null){
                System.out.println("config does not exit!");
                return;
            }
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            //解析配置文件

        }else {

            //配置文件不存在的情况
        }



        switch (config.getCommand()){

            case "default":

                //通过参数产生默认的tunnel,如果是配置文件的话可能会有多个tunnel
                Map<String, PublicTunnelConfiguration> tunnels = config.getPublicTunnelConfiguration();
                PublicTunnelConfiguration publicTunnelConfiguration = new PublicTunnelConfiguration();

                if (cmd.hasOption("subdomain")){
                    publicTunnelConfiguration.setSubDomain(cmd.getOptionValue("subdomain"));
                }

                if (cmd.hasOption("hostname")){
                    config.setServerAddress(cmd.getOptionValue("hostname"));
                    publicTunnelConfiguration.setHostName(cmd.getOptionValue("hostname"));
                }else {
                    if (config.getServerAddress()!=null && "".equalsIgnoreCase(config.getServerAddress())) {
                        publicTunnelConfiguration.setHostName(config.getServerAddress());
                    }else {
                        System.out.println("please specify one hostname");
                    }

                }

                if (cmd.hasOption("remoteport")){
                    publicTunnelConfiguration.setRemotePort(Integer.valueOf(cmd.getOptionValue("remoteport")));
                }else {
                    System.out.println("please specify one port");
                    return;
                }

                if (cmd.hasOption("localport")){
                    publicTunnelConfiguration.setLocalPort(Integer.valueOf(cmd.getOptionValue("localport")));
                }else {
                    System.out.println("please specify one port");
                    return;
                }

                tunnels.put("default", publicTunnelConfiguration);

                break;

            case "list":

                break;

            case "start":

                break;

            default:

                System.out.println("unknown command");
        }
    }
}

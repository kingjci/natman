package jc.client.core;

import jc.TCPConnection;
import jc.Pipe;
import jc.Version;
import org.apache.commons.cli.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static jc.client.core.Main.config;
import static jc.client.core.Main.consoleLock;
import static jc.client.core.Main.threadRegistry;
import static jc.client.core.Main.random;
import static jc.client.core.Main.timeFormat;

/**
 * Created by ��� on 2015/9/8.
 */
public class Utils {



    public static void Go(Runnable runnable){

        Thread thread = new Thread(runnable);
        thread.start();

    }

    public static void Join(TCPConnection c1, TCPConnection c2){

        CountDownLatch waitGroup = new CountDownLatch(2);
        Go(new Pipe(c1, c2, waitGroup));
        Go(new Pipe(c2, c1, waitGroup));
        System.out.printf("[%s][Utils]Join %s with %s\n",
                timeStamp() ,c1.Id(),  c2.Id() );

        try{
            waitGroup.await();
            c1.close();
            c2.close();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        System.out.printf("[%s][Utils Join]Disjoin %s with %s\n",
                timeStamp() ,c1.Id(),  c2.Id());

    }
    public static TCPConnection Dial(String host, String type){
        return Dial(host, 12345, type);
    }

    public static TCPConnection Dial(String host, int port, String type){

        Socket socket = null;
        TCPConnection TCPConnection = null;
        try{
            socket = new Socket();
            SocketAddress address = new InetSocketAddress(host, port);
            //连接端口，3秒钟超时。当连接本地端口的时候如果端口没有开放会出现异常connect refused
            socket.connect(address, 3*1000);
            TCPConnection = new TCPConnection(socket, type, random.getRandomString(8));
            System.out.printf("[%s][Utils]New %s connection[%s] to: %s\n",
                    timeStamp(), TCPConnection.getType(), TCPConnection.getConnectionId(), TCPConnection.getRemoteAddr());

        }catch (IOException e){
            System.out.printf("[%s][Utils]can not connect to %s:%d, perhaps this port is not open\n", timeStamp(), host, port);
            e.printStackTrace();
        }
        return TCPConnection;

    }

    public static void ShutdownThread(String threadName){

        threadRegistry.getWriteLock();
        consoleLock.lock();
        int result = threadRegistry.remove(threadName);
        if (result !=0){
            System.out.printf("remove %s fail\n", threadName);
        }else {
            System.out.printf("remove %s success\n", threadName);
        }
        consoleLock.unlock();
        threadRegistry.freeWriteLock();

    }

    public static void Console(Object... args){

        consoleLock.lock();

        Object[] objects = new Object[args.length-1];
        System.arraycopy(args, 1, objects, 0, args.length-1);
        System.out.printf(args[0] + "\n", objects);

        consoleLock.unlock();
    }

    public static void LoadConfiguration(String[] args){

        switch (args[0]){

            case "list":

                //显示配置文件中的tunnels
                break;

            case "version":
                System.out.println("natman version:"+Version.Current);
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
            BufferedReader vggggvbufferedReader = new BufferedReader(fileReader);
            //解析配置文件

        }else {

            //配置文件不存在的情况
        }



        switch (config.getCommand()){

            case "default":

                //通过参数产生默认的tunnel,如果是配置文件的话可能会有多个tunnel
                Map<String, TunnelConfiguration> tunnels = config.getTunnels();
                TunnelConfiguration tunnelConfiguration = new TunnelConfiguration();

                if (cmd.hasOption("subdomain")){
                    tunnelConfiguration.setSubDomian(cmd.getOptionValue("subdomain"));
                }

                if (cmd.hasOption("hostname")){
                    config.setServerAddr(cmd.getOptionValue("hostname"));
                    tunnelConfiguration.setHostName(cmd.getOptionValue("hostname"));
                }else {
                    if (config.getServerAddr()!=null && "".equalsIgnoreCase(config.getServerAddr())) {
                        tunnelConfiguration.setHostName(config.getServerAddr());
                    }else {
                        System.out.println("please specify one hostname");
                    }

                }

                if (cmd.hasOption("remoteport")){
                    tunnelConfiguration.setRemotePort(Integer.valueOf(cmd.getOptionValue("remoteport")));
                }else {
                    System.out.println("please specify one port");
                    return;
                }

                if (cmd.hasOption("localport")){
                    tunnelConfiguration.setLocalPort(Integer.valueOf(cmd.getOptionValue("localport")));
                }else {
                    System.out.println("please specify one port");
                    return;
                }

                tunnels.put("default", tunnelConfiguration);

                break;

            case "list":

                break;

            case "start":

                break;

            default:

                System.out.println("unknown command");
                return;

        }


        return;



    }

    public static String timeStamp(long timeMillis){

        return timeFormat.format(timeMillis);

    }

    public static String timeStamp(){

        return timeStamp(System.currentTimeMillis());
    }

}

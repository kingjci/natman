package jc.client.core;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

    private String serverAddress;
    private String clientAddress = "127.0.0.1";
    private String username;
    private String password;
    private Map<String, PublicTunnelConfiguration> publicTunnelConfigurations;

    private static String prompt =
            "Examples:\n" +
            "-localport 8080 -remoteport 8000 -server 127.0.0.1 -protocol tcp\n" +
            "-localport 8080 -remoteport 8000 -server 127.0.0.1 -subdomain aaa -protocol http\n" +
            "\n" +
            "\n" +
            "Advanced usage: natman [OPTIONS] <command> [command args] [...]\n" +
            "Commands:\n" +
            "\tnatman start [tunnel] [...]    Start tunnels by name from config file\n" +
            "\tnatman list  -config client.cfg List tunnel names from config file\n" +
            "\tnatman help                    Print help\n" +
            "\tnatman version                 Print natman version\n" +
            "\n" +
            "Examples:\n" +
            "\tnatman start www api blog pubsub\n" +
            "\tnatman version\n" +
            "\n" +
            "`";


    public Config(){
        publicTunnelConfigurations = new HashMap<>();
    }

    public String getServerAddress() {
        return serverAddress;
    }
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, PublicTunnelConfiguration> getPublicTunnelConfigurations() {
        return publicTunnelConfigurations;
    }
    public void putPublicTunnelConfiguration( PublicTunnelConfiguration publicTunnelConfiguration){
        publicTunnelConfigurations.put(
                publicTunnelConfiguration.getName(),
                publicTunnelConfiguration
        );
    }


    public static void LoadConfiguration(
            String[] args,
            Config config,
            Option option,
            Logger runtimeLogger
    ){

        Options options = new Options();
        options.addOption("config", true,"config path");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try{
            cmd = parser.parse(options, args);
        }catch (ParseException e){
            runtimeLogger.error(e.getMessage(), e);
        }

        if (cmd == null){
            runtimeLogger.error("command is null");
            return;
        }

        if (cmd.hasOption("config")){

            FileReader fileReader = null;
            BufferedReader bufferedReader = null;
            try{
                fileReader = new FileReader(cmd.getOptionValue("config"));
                bufferedReader = new BufferedReader(fileReader);

                Stack<String> items = new Stack<>();
                int count = 0;
                while (true){

                    String line = bufferedReader.readLine();
                    if (line == null){
                        break;
                    }
                    count++;

                    if (line.startsWith("#") | "".equals(line)){
                        continue;
                    }

                    switch (line){

                        case "[auth]":
                            items.push("[auth]");
                            count = ParseAuth(bufferedReader, config, count,runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [auth]");
                                System.exit(-1);
                            }
                            items.pop();
                            break;

                        case "[server]":
                            items.push("[server]");
                            count = ParseServer(bufferedReader, config, count,runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [server]");
                                System.exit(-1);
                            }
                            items.pop();
                            break;

                        case "[tcp]":
                            items.push("[tcp]");
                            count = ParseTcp(bufferedReader, config, count, runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [tcp]");
                                System.exit(-1);
                            }
                            items.pop();
                            break;

                        case "[http]":
                            items.push("[http]");
                            count = ParseHttp(bufferedReader, config, count, runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [http]");
                                System.exit(-1);
                            }
                            items.pop();
                            break;

                        case "[udp]":
                            items.push("[udp]");
                            count = ParseUDP(bufferedReader, config, count, runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [udp]");
                                System.exit(-1);
                            }
                            items.pop();
                            break;

                        default:
                            runtimeLogger.error(String.format("Unknown syntax at %s[%d]",line,count));
                            System.exit(-1);
                    }
                }
                if (!items.empty()){
                    runtimeLogger.error(
                            String.format(
                                    "Mismatch bracket %s",
                                    items.pop()
                            )
                    );
                }

                runtimeLogger.info(String.format("Read %d config lines", count));

            }catch (IOException e){

                runtimeLogger.error(String.format("Can not open file %s", cmd.getOptionValue("config")));
            }finally {

                if (fileReader != null){
                    try{
                        fileReader.close();
                    }catch (IOException e){
                        runtimeLogger.error("Error occurs when closing the config file");
                    }
                }

                if (bufferedReader != null){
                    try{
                        bufferedReader.close();
                    }catch (IOException e){
                        runtimeLogger.error("Error occurs when closing the config file");
                    }
                }
            }

        }else {
            PublicTunnelConfiguration publicTunnelConfiguration = new PublicTunnelConfiguration();
            publicTunnelConfiguration.setName("default");
            config.putPublicTunnelConfiguration(publicTunnelConfiguration);
        }

        switch (args[0]){

            case "list":

                Map<String, PublicTunnelConfiguration> publicTunnelConfigurations = config.getPublicTunnelConfigurations();
                for (Map.Entry<String, PublicTunnelConfiguration> entry:
                        publicTunnelConfigurations.entrySet()){
                    String publicTunnelName = entry.getKey();
                    PublicTunnelConfiguration publicTunnelConfiguration = entry.getValue();
                    String description;
                    switch (publicTunnelConfiguration.getProtocol()){

                        case "tcp":

                            description =
                                    String.format(
                                            "%s:%d:%d:%s",
                                            publicTunnelName,
                                            publicTunnelConfiguration.getLocalPort(),
                                            publicTunnelConfiguration.getRemotePort(),
                                            publicTunnelConfiguration.getProtocol()
                                    );
                            break;

                        case "http":

                            description =
                                    String.format(
                                            "%s:%s:%d:%d:%s",
                                            publicTunnelName,
                                            publicTunnelConfiguration.getSubDomain(),
                                            publicTunnelConfiguration.getLocalPort(),
                                            publicTunnelConfiguration.getRemotePort(),
                                            publicTunnelConfiguration.getProtocol()
                                    );
                            break;

                        case "udp":
                            description =
                                    String.format(
                                            "%s:%d:%d:%s",
                                            publicTunnelName,
                                            publicTunnelConfiguration.getLocalPort(),
                                            publicTunnelConfiguration.getRemotePort(),
                                            publicTunnelConfiguration.getProtocol()
                                    );
                            break;

                        default:

                            description =
                                    String.format(
                                            "%s:unknown tunnel",
                                            publicTunnelName
                                    );
                    }

                    System.out.println(description);
                }
                System.exit(0);

            case "version":
                System.out.println(String.format("Natman version:%f\n",option.getVersion()));
                System.exit(0);

            case "help":
                System.out.println(prompt);
                System.exit(0);

            case "":
                System.out.println(prompt);
                System.exit(0);

            default:

        }


    }

    public static int ParseAuth(BufferedReader bufferedReader, Config config,int count, Logger runtimeLogger) {


        try {

            while (true) {

                String line = bufferedReader.readLine();
                count++;

                if ("[/auth]".equalsIgnoreCase(line)){
                    break;
                }

                if (line.startsWith("#") | "".equals(line)) {
                    continue;
                }

                String[] words = line.split(":");
                if (words.length == 2){

                    Pattern pattern;
                    Matcher matcher;

                    String regexUsername = "\\S{3,}";
                    pattern = Pattern.compile(regexUsername);
                    matcher = pattern.matcher(words[0]);
                    if (!matcher.find()){
                        runtimeLogger.error(String.format("Auth config error occurs at line %d: invalid username %s",count,words[0]));
                        System.exit(-1);
                    }

                    String regexPassword = "\\S{3,}";
                    pattern = Pattern.compile(regexPassword);
                    matcher = pattern.matcher(words[1]);
                    if (!matcher.find()){
                        runtimeLogger.error(String.format("Auth config error occurs at line %d: invalid password %s",count,words[0]));
                        System.exit(-1);
                    }

                    config.setUsername(words[0]);
                    config.setPassword(words[1]);

                }else {
                    runtimeLogger.error(String.format("Auth error occurs at line %d", count));
                    System.exit(-1);
                }
            }
            return count;

        } catch (IOException e) {
            runtimeLogger.error(e.getMessage(), e);
            return 0;
        }

    }

    public static int ParseServer(BufferedReader bufferedReader, Config config,int count, Logger runtimeLogger){

        try{

            while (true){

                String line = bufferedReader.readLine();
                count++;

                if ("[/server]".equalsIgnoreCase(line)){
                    break;
                }

                if (line.startsWith("#") | "".equals(line)) {
                    continue;
                }

                String regexDomain = "(?:(?:\\d{1,3}){1,3}\\d{1,3})|(?:(?:[\\w|-]+){2,})";
                Pattern pattern = Pattern.compile(regexDomain);
                Matcher matcher = pattern.matcher(line);

                if (!matcher.find()){
                    runtimeLogger.error(
                            String.format(
                                    "Server address %s format is wrong",
                                    line
                            )
                    );
                    System.exit(-1);
                }

                config.setServerAddress(line);
            }
            return count;

        }catch (IOException e){
            runtimeLogger.error(e.getMessage(), e);
            return 0;
        }

    }

    public static int ParseTcp(BufferedReader bufferedReader, Config config,int count, Logger runtimeLogger){

        try{

            while (true){
                String line = bufferedReader.readLine();
                count++;

                if ("[/tcp]".equalsIgnoreCase(line)){
                    break;
                }

                if (line.startsWith("#") | "".equals(line)) {
                    continue;
                }

                String[] words = line.split(":");
                if (words.length == 3){

                    Pattern pattern;
                    Matcher matcher;

                    String regexName = "\\S+";
                    pattern = Pattern.compile(regexName);
                    matcher = pattern.matcher(words[0]);

                    if (!matcher.find()){
                        runtimeLogger.error(String.format("TCP config error occurs at line %d: invalid tunnel name format", count));
                        System.exit(-1);
                    }

                    String regexLocalPort = "\\d{1,5}";
                    pattern = Pattern.compile(regexLocalPort);
                    matcher = pattern.matcher(words[1]);

                    if (!matcher.find()){
                        runtimeLogger.error(String.format("TCP config error occurs at line %d: invalid local port format", count));
                        System.exit(-1);

                    }
                    int localPort = Integer.valueOf(words[1]);
                    if (localPort <= 0 | localPort > 65535){
                        runtimeLogger.error(String.format("TCP config error occurs at line %d: local port should between 1 - 65536", count));
                        System.exit(-1);
                    }

                    String regexRemotePort = "\\d{1,5}";
                    pattern = Pattern.compile(regexRemotePort);
                    matcher = pattern.matcher(words[2]);

                    if (!matcher.find()){
                        runtimeLogger.error(String.format("TCP config error occurs at line %d: invalid remote port format", count));
                        System.exit(-1);

                    }

                    int remotePort = Integer.valueOf(words[1]);
                    if (remotePort <= 0 | remotePort > 65535){
                        runtimeLogger.error(String.format("TCP config error occurs at line %d: remote port should between 1 - 65536", count));
                        System.exit(-1);
                    }

                    PublicTunnelConfiguration publicTunnelConfiguration = new PublicTunnelConfiguration();
                    publicTunnelConfiguration.setProtocol("tcp");
                    publicTunnelConfiguration.setName(words[0]);
                    publicTunnelConfiguration.setLocalPort(Integer.valueOf(words[1]));
                    publicTunnelConfiguration.setRemotePort(Integer.valueOf(words[2]));
                    config.putPublicTunnelConfiguration(publicTunnelConfiguration);

                }else {
                    runtimeLogger.error(String.format("TCP %s format is wrong at %d",line,count));
                    System.exit(-1);
                }
            }

            return count;

        }catch (IOException e){
            runtimeLogger.error(e.getMessage(), e);
            return 0;
        }
    }

    public static int ParseHttp(BufferedReader bufferedReader, Config config,int count, Logger runtimeLogger){

        try{

            while (true){
                String line = bufferedReader.readLine();
                count++;

                if ("[/http]".equalsIgnoreCase(line)){
                    break;
                }

                if (line.startsWith("#") | "".equals(line)){
                    continue;
                }

                String[] words = line.split(":");

                if ( words.length == 4){

                    Pattern pattern;
                    Matcher matcher;

                    String regexName = "\\S+";
                    pattern = Pattern.compile(regexName);
                    matcher = pattern.matcher(words[0]);

                    if (!matcher.find()){
                        runtimeLogger.error(String.format("HTTP config error occurs at line %d: invalid http name %s", count, words[0]));
                        System.exit(-1);
                    }

                    String regexSubdomain = "[\\S^\\.]+";
                    pattern = Pattern.compile(regexSubdomain);
                    matcher = pattern.matcher(words[1]);

                    if (!matcher.find()){
                        runtimeLogger.error(String.format("HTTP config error occurs at line %d: invalid subdomain %s", count, words[1]));
                        System.exit(-1);
                    }

                    String regexLocalPort = "\\d{1,5}";
                    pattern = Pattern.compile(regexLocalPort);
                    matcher = pattern.matcher(words[2]);

                    if (!matcher.find()){
                        runtimeLogger.error(String.format("HTTP config error occurs at line %d: invalid local port %S", count, words[2]));
                        System.exit(-1);
                    }

                    int localPort = Integer.valueOf(words[2]);
                    if (localPort <= 0 | localPort > 65535){
                        runtimeLogger.error(String.format("HTTP config error occurs at line %d: local port should between 1 - 65536", count));
                        System.exit(-1);
                    }

                    String regexRemotePort = "\\d{1,5}";
                    pattern = Pattern.compile(regexRemotePort);
                    matcher = pattern.matcher(words[3]);

                    if (!matcher.find()){
                        runtimeLogger.error(String.format("HTTP config error occurs at line %d: invalid remote port %s", count, words[3]));
                        System.exit(-1);

                    }
                    int remotePort = Integer.valueOf(words[3]);
                    if (remotePort <= 0 | remotePort > 65535){
                        runtimeLogger.error(String.format("HTTP config error occurs at line %d: remote port should between 1 - 65536", count));
                        System.exit(-1);
                    }


                    PublicTunnelConfiguration publicTunnelConfiguration = new PublicTunnelConfiguration();
                    publicTunnelConfiguration.setProtocol("http");
                    publicTunnelConfiguration.setName(words[0]);
                    publicTunnelConfiguration.setSubDomain(words[1]);
                    publicTunnelConfiguration.setLocalPort(Integer.valueOf(words[2]));
                    publicTunnelConfiguration.setRemotePort(Integer.valueOf(words[3]));
                    config.putPublicTunnelConfiguration(publicTunnelConfiguration);

                }else {
                    runtimeLogger.error(String.format("HTTP %s format is wrong at %d",line, count));
                    System.exit(-1);
                }
            }
            return count;

        }catch (IOException e){
            runtimeLogger.error(e.getMessage(), e);
            return 0;
        }
    }

    public static int ParseUDP(BufferedReader bufferedReader, Config config, int count, Logger runtimeLogger) {

        try {
            while (true) {
                String line = bufferedReader.readLine();
                count++;

                if ("[/udp]".equalsIgnoreCase(line)) {
                    break;
                }

                if (line.startsWith("#") | "".equals(line)) {
                    continue;
                }

                String[] words = line.split(":");
                if (words.length == 3) {

                    Pattern pattern;
                    Matcher matcher;

                    String regexName = "\\S+";
                    pattern = Pattern.compile(regexName);
                    matcher = pattern.matcher(words[0]);

                    if (!matcher.find()) {
                        runtimeLogger.error(String.format("UDP config error occurs at line %d: invalid UDP name %s", count, words[0]));
                        System.exit(-1);
                    }

                    String regexLocalPort = "\\d{1,5}";
                    pattern = Pattern.compile(regexLocalPort);
                    matcher = pattern.matcher(words[1]);

                    if (!matcher.find()) {
                        runtimeLogger.error(String.format("UDP config error occurs at line %d: invalid local port %s", count, words[1]));
                        System.exit(-1);

                    }

                    int localPort = Integer.valueOf(words[1]);
                    if (localPort <= 0 | localPort > 65535) {
                        runtimeLogger.error(String.format("UDP config error occurs at line %d: local port should between 1 - 65536", count));
                        System.exit(-1);
                    }

                    String regexRemotePort = "\\d{1,5}";
                    pattern = Pattern.compile(regexRemotePort);
                    matcher = pattern.matcher(words[2]);

                    if (!matcher.find()) {
                        runtimeLogger.error(String.format("UDP config error occurs at line %d: invalid remote port format", count));
                        System.exit(-1);
                    }

                    int remotePort = Integer.valueOf(words[1]);
                    if (remotePort <= 0 | remotePort > 65535) {
                        runtimeLogger.error(String.format("UDP config error occurs at line %d: remote port should between 1 - 65536", count));
                        System.exit(-1);
                    }

                    PublicTunnelConfiguration publicTunnelConfiguration = new PublicTunnelConfiguration();
                    publicTunnelConfiguration.setProtocol("udp");
                    publicTunnelConfiguration.setName(words[0]);
                    publicTunnelConfiguration.setLocalPort(Integer.valueOf(words[1]));
                    publicTunnelConfiguration.setRemotePort(Integer.valueOf(words[2]));
                    config.putPublicTunnelConfiguration(publicTunnelConfiguration);

                } else {
                    runtimeLogger.error(String.format("UDP %s format is wrong at %d", line, count));
                    System.exit(-1);
                }
            }
            return count;

        } catch (IOException e) {
            runtimeLogger.error(e.getMessage(), e);
            return 0;
        }
    }
}

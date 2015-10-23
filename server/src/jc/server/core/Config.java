package jc.server.core;

import jc.server.core.Users.FileUsers;
import jc.server.core.Users.MysqlUsers;
import jc.server.core.Users.Users;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

    //store config read from the config file
    private Set<String> bannedPort; // tcp://jincheng.link:8000这个形式
    private Map<Integer, Integer> httpRedirect;
    private String domain;
    private int controlPort;
    private boolean auth;
    private String usersSource;
    private String usersFile;
    private String mysqlUrl;
    private String mysqlUsername;
    private String mysqlPassword;
    private Users users;

    public Config(String[] args, Logger runtimeLogger){

        bannedPort = new HashSet<>();
        httpRedirect = new HashMap<>();
        domain = "jincheng.link"; // Default value
        controlPort = 12345;
        auth = false;

        LoadConfiguration(args, runtimeLogger);
        LoadUsers(runtimeLogger);

    }

    public String getDomain() {
        return domain;
    }
    public int getControlPort() {
        return controlPort;
    }
    public Set<String> getBannedPort() {
        return bannedPort;
    }
    public boolean isAuth() {
        return auth;
    }
    public Map<Integer,Integer> getHttpRedirect(){
        return  httpRedirect;
    }

    public Users getUsers() {
        return users;
    }

    private void LoadConfiguration(
            String[] args,
            Logger runtimeLogger){

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

                        case "[domain]":
                            items.push("[domain]");
                            count = ParseDomain(bufferedReader,count, runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [domain]");
                                System.exit(-1);
                            }
                            items.pop();
                            break;

                        case "[port]":
                            items.push("[port]");
                            count = ParsePort(bufferedReader, count, runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [port]");
                                System.exit(-1);
                            }
                            items.pop();
                            break;

                        case "[auth]":
                            items.push("[auth]");
                            count = ParseAuth(bufferedReader,count, runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [Auth]");
                                System.exit(-1);
                            }
                            items.pop();
                            break;

                        case "[users-file]":
                            items.push("[users-file]");
                            count = ParseUsersFile(bufferedReader, count, runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [users-file]");
                                System.exit(-1);
                            }
                            items.pop();
                            break;

                        case "[users-mysql]":
                            items.push("[users-mysql]");
                            count = ParseUsersMysql(bufferedReader,count, runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [users-mysql]");
                                System.exit(-1);
                            }
                            items.pop();
                            break;

                        case "[banned-port]":
                            items.push("[banned-port]");
                            count = ParseBannedPort(bufferedReader,count, runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [banned-port]");
                                System.exit(-1);
                            }
                            items.pop();
                            break;

                        case "[http-redirect]":
                            items.push("[http-redirect]");
                            count = ParseHTTPRedirect(bufferedReader,count, runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [http-redirect]");
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
                    if (!items.empty()){
                        runtimeLogger.error(
                                String.format(
                                        "Mismatch bracket %s",
                                        items.pop()
                                )
                        );
                    }
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
            runtimeLogger.error("Please give config file, example: -config server.cfg");
        }

    }

    private int ParseDomain(BufferedReader bufferedReader, int count, Logger runtimeLogger){

        try{

            while (true){

                String line = bufferedReader.readLine();
                count++;

                if ("[/domain]".equalsIgnoreCase(line)){
                    break;
                }

                if (line.startsWith("#") | "".equals(line)) {
                    continue;
                }

                String regexDomain = "(?:(?:\\d{1,3}){1,3}\\d{1,3})|(?:(?:[\\w-]){2,})";
                Pattern pattern = Pattern.compile(regexDomain);
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()){
                    domain = line;
                }else {
                    runtimeLogger.error(String.format("Domain %s format is wrong",line));
                    System.exit(-1);
                }
            }
            return count;

        }catch (IOException e){
            runtimeLogger.error(e.getMessage(), e);
            return 0;
        }
    }

    private int ParsePort(BufferedReader bufferedReader,int count, Logger runtimeLogger){

        try{

            while (true) {

                String line = bufferedReader.readLine();
                count++;

                if ("[/port]".equalsIgnoreCase(line)) {
                    break;
                }

                if (line.startsWith("#") | "".equals(line)) {
                    continue;
                }

                Pattern pattern;
                Matcher matcher;

                String regexPort = "\\d{1,5}";
                pattern = Pattern.compile(regexPort);
                matcher = pattern.matcher(line);

                if (!matcher.find()) {
                    runtimeLogger.error(String.format("Port config error occurs at line %d: invalid port", count));
                    System.exit(-1);
                }
                int port = Integer.valueOf(line);
                if (port <= 0 | port > 65535) {
                    runtimeLogger.error(String.format("Port config error occurs at line %d: port should between 1 - 65536", count));
                    System.exit(-1);
                }
                controlPort = port;
            }

            return count;

        }catch (IOException e){
            runtimeLogger.error(e.getMessage(), e);
            return 0;
        }
    }

    private int ParseAuth(BufferedReader bufferedReader, int count, Logger runtimeLogger){

        try{

            while (true){

                String line = bufferedReader.readLine();
                count++;

                if ("[/auth]".equalsIgnoreCase(line)){
                    break;
                }

                if (line.startsWith("#") | "".equals(line)) {
                    continue;
                }

                String[] words = line.split(":");
                if (words.length == 2 | words.length == 1){

                    Pattern pattern;
                    Matcher matcher;

                    String regexAuth = "\\S+";
                    pattern = Pattern.compile(regexAuth);
                    matcher = pattern.matcher(words[0]);

                    if (!matcher.find()){
                        runtimeLogger.error(String.format("Auth config error occurs at line %d: invalid auth format", count));
                        System.exit(-1);
                    }

                    auth = Boolean.valueOf(words[0]);

                    String regexSource = "\\S+";
                    pattern = Pattern.compile(regexSource);
                    matcher = pattern.matcher(words[0]);

                    if (!matcher.find()){
                        runtimeLogger.error(String.format("Auth config error occurs at line %d: invalid auth format", count));
                        System.exit(-1);
                    }

                    if (words.length == 2){
                        usersSource = words[1];
                    }

                }else {
                    runtimeLogger.error(String.format("Auth %s format is wrong at %d",line,count));
                    System.exit(-1);
                }
            }

            return count;

        }catch (IOException e){
            runtimeLogger.error(e.getMessage(), e);
            return 0;
        }
    }

    private int ParseUsersFile(BufferedReader bufferedReader,int count, Logger runtimeLogger) {

        try {

            while (true) {

                String line = bufferedReader.readLine();
                count++;

                if ("[/users-file]".equalsIgnoreCase(line)) {
                    break;
                }

                if (line.startsWith("#") | "".equals(line)) {
                    continue;
                }

                usersFile = line;
            }

            return count;

        } catch (IOException e) {
            runtimeLogger.error(e.getMessage(), e);
            return 0;
        }
    }

    private int ParseUsersMysql(BufferedReader bufferedReader,int count, Logger runtimeLogger){

        try{

            while (true) {

                String line = bufferedReader.readLine();
                count++;

                if ("[/users-mysql]".equalsIgnoreCase(line)) {
                    break;
                }

                if (line.startsWith("#") | "".equals(line)) {
                    continue;
                }

                String[] words = line.split(",");
                if (words.length == 3) {

                    Pattern pattern;
                    Matcher matcher;

                    String regexUrl = "\\S+";
                    pattern = Pattern.compile(regexUrl);
                    matcher = pattern.matcher(words[0]);

                    if (!matcher.find()) {
                        runtimeLogger.error(String.format("users-mysql config error occurs at line %d: invalid mysql url", count));
                        System.exit(-1);
                    }

                   mysqlUrl = words[0];

                    String regexUsername = "\\S+";
                    pattern = Pattern.compile(regexUsername);
                    matcher = pattern.matcher(words[1]);

                    if (!matcher.find()) {
                        runtimeLogger.error(String.format("users-mysql config error occurs at line %d: invalid mysql username", count));
                        System.exit(-1);
                    }

                    mysqlUsername = words[1];

                    String regexPassword = "\\S+";
                    pattern = Pattern.compile(regexPassword);
                    matcher = pattern.matcher(words[2]);
                    if (!matcher.find()) {
                        runtimeLogger.error(String.format("users-mysql config error occurs at line %d: invalid mysql password", count));
                        System.exit(-1);
                    }

                    mysqlPassword = words[2];
                } else {
                    runtimeLogger.error(String.format("users-mysql %s format is wrong at %d", line, count));
                    System.exit(-1);
                }
            }

            return count;

        }catch (IOException e){
            runtimeLogger.error(e.getMessage(), e);
            return 0;
        }

    }

    private int ParseBannedPort(BufferedReader bufferedReader,int count, Logger runtimeLogger){

        try{

            while (true){

                String line = bufferedReader.readLine();
                count++;

                if ("[/banned-port]".equalsIgnoreCase(line)){
                    break;
                }

                if (line.startsWith("#") | "".equals(line)) {
                    continue;
                }

                String[] words = line.split(":");

                if (words.length == 2){


                    String regexProtocol = "\\w{1,5}";
                    Pattern pattern = Pattern.compile(regexProtocol);
                    Matcher matcher = pattern.matcher(words[0]);

                    if (!matcher.find()){
                        runtimeLogger.error(String.format("Banned-port config error occurs at line %d: invalid mysql password", count));
                        System.exit(-1);
                    }

                    int port = Integer.valueOf(words[1]);

                    if (port <= 0 | port > 65535) {
                        runtimeLogger.error(String.format("Banned port config error occurs at line %d: port should between 1 - 65536", count));
                        System.exit(-1);
                    }
                    bannedPort.add(
                            String.format(
                                    "%s://%s:%d",
                                    "tcp",
                                    domain,
                                    port
                            )
                    );

                }else {
                    runtimeLogger.error(String.format("Banned port %s format is wrong at %d", line, count));
                    System.exit(-1);
                }
            }
            return count;

        }catch (IOException e){
            runtimeLogger.error(e.getMessage(), e);
            return 0;
        }
    }

    private int ParseHTTPRedirect(BufferedReader bufferedReader, int count, Logger runtimeLogger){

        try{

            while (true) {

                String line = bufferedReader.readLine();
                count++;

                if ("[/http-redirect]".equalsIgnoreCase(line)) {
                    break;
                }

                if (line.startsWith("#") | "".equals(line)) {
                    continue;
                }

                String[] words = line.split(":");
                if (words.length == 2) {

                    Pattern pattern;
                    Matcher matcher;

                    String regexVirtualPort = "\\d{1,5}";
                    pattern = Pattern.compile(regexVirtualPort);
                    matcher = pattern.matcher(words[0]);

                    if (!matcher.find()) {
                        runtimeLogger.error(String.format("http-redirect config error occurs at line %d: invalid mysql url", count));
                        System.exit(-1);
                    }

                    int virtualPort = Integer.valueOf(words[0]);
                    if (virtualPort <=0 | virtualPort >=65536){
                        runtimeLogger.error(
                                "Http redirect virtual port should between 1 and 65536"
                                );
                        System.exit(-1);
                    }

                    String regexRealPort = "\\d{1,5}";
                    pattern = Pattern.compile(regexRealPort);
                    matcher = pattern.matcher(words[1]);

                    if (!matcher.find()) {
                        runtimeLogger.error(String.format("http-redirect config error occurs at line %d: invalid mysql url", count));
                        System.exit(-1);
                    }

                    int realPort = Integer.valueOf(words[1]);
                    if (realPort <=0 | realPort >=65536){
                        runtimeLogger.error(
                                "Http redirect realPort port should between 1 and 65536"
                        );
                        System.exit(-1);
                    }

                    httpRedirect.put(virtualPort, realPort);

                } else {
                    runtimeLogger.error(String.format("http-redirect %s format is wrong at %d", line, count));
                    System.exit(-1);
                }
            }

            return count;

        }catch (IOException e){
            runtimeLogger.error(e.getMessage(), e);
            return 0;
        }
    }

    private void LoadUsers(Logger runtimeLogger){

        if (auth) {

            switch (usersSource) {
                case "file":
                    users = new FileUsers(usersFile, runtimeLogger);
                    break;
                case "mysql":
                    users = new MysqlUsers(mysqlUrl, mysqlUsername, mysqlPassword, runtimeLogger);
                    break;
                default:
                    runtimeLogger.error(String.format("Unknown users sources %s", usersFile));
            }

        }
    }
}

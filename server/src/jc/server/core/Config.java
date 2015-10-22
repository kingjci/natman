package jc.server.core;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

    //用来保存，从服务器配置文件中读取的配置

    private Set<String> bannedPort; // tcp://jincheng.link:8000这个形式
    private String domain = "jincheng.link";
    private int controlPort = 12345;
    private boolean auth = false;
    private String userSource;
    private String usersFile;
    private String mysqlUrl;
    private String mysqlUsername;
    private String mysqlPassword;


    public Config(){
        bannedPort = new HashSet<>();

    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getControlPort() {
        return controlPort;
    }

    public void setControlPort(int controlPort) {
        this.controlPort = controlPort;
    }

    public Set<String> getBannedPort() {
        return bannedPort;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public String getUserSource() {
        return userSource;
    }

    public void setUserSource(String userSource) {
        this.userSource = userSource;
    }

    public String getUsersFile() {
        return usersFile;
    }

    public void setUsersFile(String usersFile) {
        this.usersFile = usersFile;
    }

    public String getMysqlUrl() {
        return mysqlUrl;
    }

    public void setMysqlUrl(String mysqlUrl) {
        this.mysqlUrl = mysqlUrl;
    }

    public String getMysqlUsername() {
        return mysqlUsername;
    }

    public void setMysqlUsername(String mysqlUsername) {
        this.mysqlUsername = mysqlUsername;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public void setMysqlPassword(String mysqlPassword) {
        this.mysqlPassword = mysqlPassword;
    }

    public void setBannedPort(Set<String> bannedPort) {
        this.bannedPort = bannedPort;
    }

    public static void LoadConfiguration(
            String[] args,
            Config config,
            Option option,
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
                            count = ParseDomain(bufferedReader, config, count, runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [domain]");
                                System.exit(-1);
                            }
                            items.pop();
                            break;

                        case "[port]":
                            items.push("[port]");
                            count = ParsePort(bufferedReader, config, count, runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [port]");
                                System.exit(-1);
                            }
                            items.pop();
                            break;

                        case "[auth]":
                            items.push("[auth]");
                            count = ParseAuth(bufferedReader, config, count, runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [Auth]");
                                System.exit(-1);
                            }
                            items.pop();
                            break;

                        case "[users-file]":
                            items.push("[users-file]");
                            count = ParseUsersFile(bufferedReader, config, count, runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [users-file]");
                                System.exit(-1);
                            }
                            items.pop();
                            break;

                        case "[users-mysql]":
                            items.push("[users-mysql]");
                            count = ParseUsersMysql(bufferedReader, config, count, runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [users-mysql]");
                                System.exit(-1);
                            }
                            items.pop();
                            break;

                        case "[banned-port]":
                            items.push("[banned-port]");
                            count = ParseBannedPort(bufferedReader, config, count, runtimeLogger);
                            if (count == 0){
                                runtimeLogger.error("Error occurs when parsing [banned-port]");
                                System.exit(-1);
                            }
                            items.pop();
                            break;

                        default:
                            runtimeLogger.error(String.format("Unknown syntax at %s[%d]",line,count));
                            System.exit(-1);
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


        }

    }


    public static int ParseDomain(BufferedReader bufferedReader, Config config,int count, Logger runtimeLogger){

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
                    config.setDomain(line);
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

    public static int ParsePort(BufferedReader bufferedReader, Config config,int count, Logger runtimeLogger){

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

                config.setControlPort(port);
            }

            return count;

        }catch (IOException e){
            runtimeLogger.error(e.getMessage(), e);
            return 0;
        }
    }

    public static int ParseAuth(BufferedReader bufferedReader, Config config,int count, Logger runtimeLogger){

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
                if (words.length == 2){

                    Pattern pattern;
                    Matcher matcher;

                    String regexAuth = "\\S+";
                    pattern = Pattern.compile(regexAuth);
                    matcher = pattern.matcher(words[0]);

                    if (!matcher.find()){
                        runtimeLogger.error(String.format("Auth config error occurs at line %d: invalid auth format", count));
                        System.exit(-1);
                    }

                    config.setAuth(Boolean.valueOf(words[0]));

                    String regexSource = "\\S+";
                    pattern = Pattern.compile(regexSource);
                    matcher = pattern.matcher(words[0]);

                    if (!matcher.find()){
                        runtimeLogger.error(String.format("Auth config error occurs at line %d: invalid auth format", count));
                        System.exit(-1);
                    }

                    config.setUserSource(words[1]);

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

    public static int ParseUsersFile(BufferedReader bufferedReader, Config config,int count, Logger runtimeLogger) {

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

                config.setUsersFile(line);
            }

            return count;

        } catch (IOException e) {
            runtimeLogger.error(e.getMessage(), e);
            return 0;
        }
    }

    public static int ParseUsersMysql(BufferedReader bufferedReader, Config config,int count, Logger runtimeLogger){

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

                String[] words = line.split(":");
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

                    config.setMysqlUrl(words[0]);

                    String regexUsername = "\\S+";
                    pattern = Pattern.compile(regexUsername);
                    matcher = pattern.matcher(words[1]);

                    if (!matcher.find()) {
                        runtimeLogger.error(String.format("users-mysql config error occurs at line %d: invalid mysql username", count));
                        System.exit(-1);
                    }

                    config.setMysqlUsername(words[1]);

                    String regexPassword = "\\S+";
                    pattern = Pattern.compile(regexPassword);
                    matcher = pattern.matcher(words[2]);
                    if (!matcher.find()) {
                        runtimeLogger.error(String.format("users-mysql config error occurs at line %d: invalid mysql password", count));
                        System.exit(-1);
                    }

                    config.setMysqlUrl(words[2]);
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

    public static int ParseBannedPort(BufferedReader bufferedReader, Config config,int count, Logger runtimeLogger){

        try{

            Set<String> bannedPort = config.getBannedPort();

            while (true){

                String line = bufferedReader.readLine();
                count++;

                if ("[/banned-port]".equalsIgnoreCase(line)){
                    break;
                }

                if (line.startsWith("#") | "".equals(line)) {
                    continue;
                }

                String regexPort = "\\d{1,5}";
                Pattern pattern = Pattern.compile(regexPort);
                Matcher matcher = pattern.matcher(line);

                if (!matcher.find()){
                    runtimeLogger.error(String.format("banned-port config error occurs at line %d: invalid mysql password", count));
                    System.exit(-1);
                }

                int port = Integer.valueOf(line);

                if (port <= 0 | port > 65535) {
                    runtimeLogger.error(String.format("Banned port config error occurs at line %d: port should between 1 - 65536", count));
                    System.exit(-1);
                }
                bannedPort.add(
                        String.format(
                                "%s://%s:%d",
                                "tcp",
                                config.getDomain(),
                                port
                        )
                );
            }
            return count;

        }catch (IOException e){
            runtimeLogger.error(e.getMessage(), e);
            return 0;
        }
    }
}

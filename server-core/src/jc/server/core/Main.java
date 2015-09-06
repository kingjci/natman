package jc.server.core;

import jc.server.core.connection.ControlConnection;
import jc.server.core.entity.Administrator;
import jc.server.core.entity.User;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        ResourceBundle config = ResourceBundle.getBundle("config", Locale.getDefault());
        Map<Integer, Integer> publicPortMap = new HashMap<Integer, Integer>();


        Administrator administrator = new Administrator(
                config.getString("administrator"),
                config.getString("password"));

        Map<String, String> users = new HashMap<String, String>();
        Set<String> authorisedUsers = new HashSet<String>();
        try{
            Scanner scanner = new Scanner(new File(config.getString("userlist")));
            while (scanner.hasNext()){
                String line = scanner.nextLine();
                if (line.startsWith("#")){
                    continue;
                }

                String[] usernameAndPassword = line.split("\\s+");
                users.put(usernameAndPassword[0], usernameAndPassword[1]);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("load users successfully");

        Thread controlConnectionThread = new Thread(new ControlConnection(users, authorisedUsers, publicPortMap));
        controlConnectionThread.start();
        System.out.println("control connection start successfully");


















    }

}

package jc.server.core.Users;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by 金成 on 2015/10/23.
 */
public class FileUsers implements Users{

    private String filePath;
    private Map<String, String> usernameAndPassword;

    public FileUsers(String filePath, Logger runtimeLogger){

        this.filePath = filePath;
        usernameAndPassword = new HashMap<>();
        Reader reader = null;
        BufferedReader bufferedReader = null;
        try{

            reader = new FileReader(filePath);
            bufferedReader = new BufferedReader(reader);
            int count = 0;
            while (true){
                String line = bufferedReader.readLine();
                if (line == null){
                    break;
                }
                count++;

                String[] words = line.split(" ");
                if (words.length == 2){
                    usernameAndPassword.put(words[0],words[1]);
                }else {
                    runtimeLogger.error(
                            String.format(
                                    "Users error at %d",
                                    count
                            )
                    );
                }
            }
        }catch (FileNotFoundException e){
            runtimeLogger.error(e.getMessage(),e);
        }catch (IOException e){
            runtimeLogger.error(e.getMessage(), e);
        }finally {

            if (reader != null){
                try{
                    reader.close();
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
    }

    @Override
    public boolean auth(String username, String password) {
        return password.equalsIgnoreCase(usernameAndPassword.get(username));
    }
}

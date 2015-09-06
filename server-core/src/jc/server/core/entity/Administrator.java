package jc.server.core.entity;

/**
 * Created by ½ð³É on 2015/9/2.
 */
public class Administrator {

    private String username;
    private String password;

    public Administrator(String username, String password){
        this.username = username;
        this.password = password;
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
}

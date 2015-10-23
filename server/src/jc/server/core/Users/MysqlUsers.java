package jc.server.core.Users;

import org.apache.log4j.Logger;

import java.sql.*;

/**
 * Created by 金成 on 2015/10/23.
 */
public class MysqlUsers implements Users{

    private Logger runtimeLogger;
    private Connection connection;
    private PreparedStatement preparedStatement;

    public MysqlUsers(String url, String username, String password, Logger runtimeLogger){
        this.runtimeLogger= runtimeLogger;

        try{
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            preparedStatement =
                    connection.prepareStatement("select COUNT(*) from users where username = ? and password = ?");
        }catch (ClassNotFoundException e){
            runtimeLogger.error(e.getMessage(),e);
        }catch (SQLException e){
            runtimeLogger.error(e.getMessage(),e);
        }

    }

    @Override
    public boolean auth(String username, String password) {

        int count = 0;
        try{
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            count = resultSet.getInt(1);
        }catch (SQLException e){
            runtimeLogger.error(e.getMessage(),e);
        }

        return count != 0;
    }
}

package jc.message;

/**
 * Created by 金成 on 2015/9/8.
 */
public class Auth implements Message {

    private String Version; //当前版本
    private String MMversion; //major minor client version
    private String User;
    private String Password;
    private String OS;
    private String Arch;
    private String ClientId; //当客户端开始一个新的session的时候为空



    @Override
    public String getMessageType() {
        return "Auth";
    }
}

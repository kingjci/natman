package jc.message;

/**
 * Created by ��� on 2015/9/8.
 */
public class Auth implements Message {

    private String Version; //��ǰ�汾
    private String MMversion; //major minor client version
    private String User;
    private String Password;
    private String OS;
    private String Arch;
    private String ClientId; //���ͻ��˿�ʼһ���µ�session��ʱ��Ϊ��



    @Override
    public String getMessageType() {
        return "Auth";
    }
}

package jc.message;

/**
 * Created by ��� on 2015/9/8.
 */
public class AuthRequest implements Message {

    private String Version; //��ǰ�汾
    private String MMversion; //major minor client version
    private String User;
    private String Password;
    private String OS;
    private String Arch;
    private String ClientId; //���ͻ��˿�ʼһ���µ�session��ʱ��Ϊ��

    public String getVersion() {
        return Version;
    }

    public void setVersion(String version) {
        Version = version;
    }

    public String getMMversion() {
        return MMversion;
    }

    public void setMMversion(String MMversion) {
        this.MMversion = MMversion;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getOS() {
        return OS;
    }

    public void setOS(String OS) {
        this.OS = OS;
    }

    public String getArch() {
        return Arch;
    }

    public void setArch(String arch) {
        Arch = arch;
    }

    public String getClientId() {
        return ClientId;
    }

    public void setClientId(String clientId) {
        ClientId = clientId;
    }

    @Override
    public String getMessageType() {
        return "AuthRequest";
    }
}

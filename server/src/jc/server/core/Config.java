package jc.server.core;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by 金成 on 2015/10/15.
 */
public class Config {

    //用来保存，从服务器配置文件中读取的配置

    private Set<String> bannedPort; // tcp://jincheng.link:8000这个形式
    private String domain;

    public Config(){
        bannedPort = new HashSet<>();

    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Set<String> getBannedPort() {
        return bannedPort;
    }

    public void setBannedPort(Set<String> bannedPort) {
        this.bannedPort = bannedPort;
    }
}

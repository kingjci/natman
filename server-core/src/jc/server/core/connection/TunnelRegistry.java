package jc.server.core.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by 金成 on 2015/9/8.
 */
public class TunnelRegistry {

    private Map<String, Tunnel> tunnels;
    private ReadWriteLock readWriteLock;

    public TunnelRegistry(){
        this.tunnels = new HashMap<String, Tunnel>();
        this.readWriteLock = new ReentrantReadWriteLock(false);
    }

    public int Register(String url, Tunnel tunnel){

        readWriteLock.writeLock().lock();

        if (tunnels.get(url) != null){
            System.out.printf("The tunnel %s is already registered.\n", url);
            return -1;
        }

        tunnels.put(url, tunnel);

        readWriteLock.writeLock().unlock();

        return 0;
    }

    public int Delete(String url){

        readWriteLock.writeLock().lock();
        Tunnel tunnel = tunnels.remove(url);
        readWriteLock.writeLock().unlock();

        if (tunnel != null){
            return 0; //正常删除
        }else {
            return 1; //请求删除的tunnel不存在
        }
    }

}

package jc.server.core.PublicTunnel;

import jc.server.core.PublicTunnel.PublicTunnel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by 金成 on 2015/9/8.
 */
public class PublicTunnelRegistry {

    private Map<String, PublicTunnel> tunnels;
    private ReadWriteLock readWriteLock;

    public PublicTunnelRegistry(){
        this.tunnels = new HashMap<>();
        this.readWriteLock = new ReentrantReadWriteLock(false);
    }

    public void register(String url, PublicTunnel publicTunnel){

        //返回-1代表这个tunnel已经存在，0正常添加

        readWriteLock.writeLock().lock();

        if (tunnels.get(url) != null){
            System.out.printf("The tunnel %s is already registered.\n", url);
            return ;
        }

        tunnels.put(url, publicTunnel);

        readWriteLock.writeLock().unlock();
    }

    public PublicTunnel get(String url) {

        readWriteLock.readLock().lock();
        PublicTunnel publicTunnel = tunnels.get(url);
        readWriteLock.writeLock().unlock();

        return publicTunnel;
    }

    public int delete(String url) {

        readWriteLock.writeLock().lock();
        PublicTunnel publicTunnel = tunnels.remove(url);
        readWriteLock.writeLock().unlock();

        return publicTunnel != null ? 0:1; //返回0正常删除，返回1删除tunnel不存在
    }



}

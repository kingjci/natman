package jc.server.core.registry;

import jc.server.core.PublicTunnel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by 金成 on 2015/9/8.
 */
public class TunnelRegistry implements Registry<PublicTunnel>{

    private Map<String, PublicTunnel> tunnels;
    private ReadWriteLock readWriteLock;

    public TunnelRegistry(){
        this.tunnels = new HashMap<String, PublicTunnel>();
        this.readWriteLock = new ReentrantReadWriteLock(false);
    }

    @Override
    public int register(String url, PublicTunnel publicTunnel){

        //返回-1代表这个tunnel已经存在，0正常添加

        readWriteLock.writeLock().lock();

        if (tunnels.get(url) != null){
            System.out.printf("The tunnel %s is already registered.\n", url);
            return -1;
        }

        tunnels.put(url, publicTunnel);

        readWriteLock.writeLock().unlock();

        return 0;
    }

    @Override
    public PublicTunnel get(String url) {

        readWriteLock.readLock().lock();
        PublicTunnel publicTunnel = tunnels.get(url);
        readWriteLock.writeLock().unlock();

        return publicTunnel;
    }

    @Override
    public int delete(String url) {

        readWriteLock.writeLock().lock();
        PublicTunnel publicTunnel = tunnels.remove(url);
        readWriteLock.writeLock().unlock();

        return publicTunnel != null ? 0:1; //返回0正常删除，返回1删除tunnel不存在
    }



}

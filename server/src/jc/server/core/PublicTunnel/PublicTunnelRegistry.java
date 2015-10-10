package jc.server.core.PublicTunnel;

import jc.server.core.PublicTunnel.PublicTunnel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by 金成 on 2015/9/8.
 */
public class PublicTunnelRegistry {

    private Map<String, PublicTunnel> tunnels;

    private Map<String, List<String>> clientIdToPublicUrl;

    private ReadWriteLock readWriteLock;

    public PublicTunnelRegistry(){
        this.tunnels = new HashMap<>();
        this.clientIdToPublicUrl = new HashMap<>();
        this.readWriteLock = new ReentrantReadWriteLock(false);
    }

    public void register(String clientId,String url, PublicTunnel publicTunnel){

        //返回-1代表这个tunnel已经存在，0正常添加

        readWriteLock.writeLock().lock();

        if (tunnels.get(url) != null){
            System.out.printf("The tunnel %s is already registered.\n", url);
            return ;
        }

        tunnels.put(url, publicTunnel);
        if (clientIdToPublicUrl.get(clientId) != null){
            List<String> publicUrls = clientIdToPublicUrl.get(clientId);
            publicUrls.add(url);
        }else {
            List<String> publicUrls = new LinkedList<>();
            publicUrls.add(url);
            clientIdToPublicUrl.put(clientId, publicUrls);
        }

        readWriteLock.writeLock().unlock();
    }

    public PublicTunnel get(String url) {

        readWriteLock.readLock().lock();
        PublicTunnel publicTunnel = tunnels.get(url);
        readWriteLock.readLock().unlock();

        return publicTunnel;
    }

    public void delete(String clientId) {

        readWriteLock.writeLock().lock();
        List<String> publicUrls = clientIdToPublicUrl.get(clientId);
        if (publicUrls != null){

            for (String publicUrl: publicUrls){
                PublicTunnel publicTunnel = tunnels.remove(publicUrl);
                publicTunnel.close();
            }

        }

        readWriteLock.writeLock().unlock();

    }



}

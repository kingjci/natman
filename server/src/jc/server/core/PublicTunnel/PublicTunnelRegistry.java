package jc.server.core.PublicTunnel;

import jc.server.core.Config;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PublicTunnelRegistry {

    private Map<String, PublicTunnel> tunnels;
    private Map<String, List<String>> clientIdToPublicUrl;
    private ReadWriteLock readWriteLock;
    private Logger runtimeLogger;
    private Logger accessLogger;
    private Set<String> bannedPort;

    public PublicTunnelRegistry(Logger runtimeLogger, Logger accessLogger, Config config){
        this.runtimeLogger = runtimeLogger;
        this.accessLogger = accessLogger;
        this.bannedPort = config.getBannedPort();
        this.tunnels = new HashMap<>();
        this.clientIdToPublicUrl = new HashMap<>();
        this.readWriteLock = new ReentrantReadWriteLock(false);
    }

    public String register(String clientId,PublicTunnel publicTunnel){

        String publicUrl = publicTunnel.getPublicUrl();

        readWriteLock.writeLock().lock();

        if (tunnels.get(publicUrl) != null){

            String result = String.format("The public tunnel %s is already registered.", publicUrl);
            accessLogger.info(result);
            return result;
        }

        if (bannedPort.contains(publicUrl)){

            String result =
                    String.format("The public tunnel %s is banned by the administrator.", publicUrl);
            accessLogger.info(result);

            return result;

        }

        tunnels.put(publicUrl, publicTunnel);
        if (clientIdToPublicUrl.get(clientId) != null){

            List<String> publicUrlsOfClientId = clientIdToPublicUrl.get(clientId);
            publicUrlsOfClientId.add(publicUrl);

        }else {

            List<String> publicUrlsOfClientId = new LinkedList<>();
            publicUrlsOfClientId.add(publicUrl);
            clientIdToPublicUrl.put(clientId, publicUrlsOfClientId);

        }

        readWriteLock.writeLock().unlock();

        accessLogger.info(
                String.format("Register public tunnel %s successfully", publicUrl)
        );
        return "success";
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

        runtimeLogger.info(
                String.format(
                        "Public tunnels of %s closed",
                        clientId
                )
        );

        readWriteLock.writeLock().unlock();

    }



}

package jc.server.core.PublicTunnel.HTTP;

import jc.Random;
import jc.server.core.Config;
import jc.server.core.ControlConnection.ControlConnection;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PublicHTTPTunnelRegistry {

    private Map<Integer, PublicHTTPTunnel> httpTunnels;
    private Map<String, List<Integer>> clientIdToPort;
    private ReadWriteLock readWriteLock;
    private Logger runtimeLogger;
    private Logger accessLogger;
    private Set<String> bannedPort;
    private String mainDomain;
    private Random random;

    public PublicHTTPTunnelRegistry(Random random ,Logger runtimeLogger, Logger accessLogger, Config config){
        this.runtimeLogger = runtimeLogger;
        this.accessLogger = accessLogger;
        this.bannedPort = config.getBannedPort();
        this.httpTunnels = new HashMap<>();
        this.clientIdToPort = new HashMap<>();
        this.readWriteLock = new ReentrantReadWriteLock(false);
        this.mainDomain = config.getDomain();
        this.random = random;
    }

    public String register(int port,String subdomain, ControlConnection controlConnection){

        String clientId = controlConnection.getClientId();
        String publicUrl =
                String.format(
                        "http://%s.%s",
                        subdomain,
                        mainDomain
                );

        readWriteLock.writeLock().lock();

        if (bannedPort.contains(publicUrl)){

            String result =
                    String.format("The public tunnel %s is banned by the administrator.", publicUrl);
            accessLogger.info(result);

            return result;

        }

        if (!httpTunnels.containsKey(port)){
            PublicHTTPTunnel publicHTTPTunnel =
                    new PublicHTTPTunnel(port ,random, runtimeLogger, accessLogger);
            httpTunnels.put(port, publicHTTPTunnel);
        }

        PublicHTTPTunnel publicHTTPTunnel = httpTunnels.get(port);
        publicHTTPTunnel.register(publicUrl, controlConnection);

        if (clientIdToPort.get(clientId) != null){
            List<Integer> httpPortsOfClientId = clientIdToPort.get(clientId);
            httpPortsOfClientId.add(port);
        }else {
            List<Integer> httpPortsOfClientId = new LinkedList<>();
            httpPortsOfClientId.add(port);
            clientIdToPort.put(clientId, httpPortsOfClientId);
        }

        readWriteLock.writeLock().unlock();

        return "success";
    }

    public String delete(String clientId){

        readWriteLock.writeLock().lock();
        List<Integer> httpPorts = clientIdToPort.get(clientId);
        if (httpPorts == null){
            runtimeLogger.info(
                    String.format(
                            "Fail to get http ports of %s",
                            clientId
                    )
            );

            return "failure";
        }

        for (int httpPort : httpPorts){
            PublicHTTPTunnel publicHTTPTunnel = httpTunnels.get(httpPort);
            publicHTTPTunnel.delete(clientId);
        }

        runtimeLogger.info(
                String.format(
                        "Success to delete http tunnels of %s",
                        clientId
                )
        );

        readWriteLock.writeLock().unlock();

        return "success";

    }
}


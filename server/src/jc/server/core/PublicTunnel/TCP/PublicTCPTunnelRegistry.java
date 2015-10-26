package jc.server.core.PublicTunnel.TCP;

import jc.Random;
import jc.server.core.Config;
import jc.server.core.ControlConnection.ControlConnection;
import jc.server.core.PublicTunnel.Result.PublicTunnelRegisterResult;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PublicTCPTunnelRegistry {

    private Map<String, PublicTCPTunnel> tcpTunnels;
    private Map<String, List<String>> clientIdToPublicUrl;
    private ReadWriteLock readWriteLock;
    private Logger runtimeLogger;
    private Logger accessLogger;
    private Set<String> bannedPort;
    private Config config;
    private Random random;

    public PublicTCPTunnelRegistry(Logger runtimeLogger, Logger accessLogger, Config config, Random random){
        this.runtimeLogger = runtimeLogger;
        this.accessLogger = accessLogger;
        this.bannedPort = config.getBannedPort();
        this.config = config;
        this.random = random;
        this.tcpTunnels = new HashMap<>();
        this.clientIdToPublicUrl = new HashMap<>();
        this.readWriteLock = new ReentrantReadWriteLock(false);
    }

    public PublicTunnelRegisterResult register(int port, ControlConnection controlConnection){

        PublicTunnelRegisterResult registerResult = new PublicTunnelRegisterResult();
        String result;

        String clientId = controlConnection.getClientId();
        String publicUrl =
                String.format("%s://%s:%d",
                        "tcp",
                        config.getDomain(),
                        port
                );

        registerResult.setPublicUrl(publicUrl);

        if (bannedPort.contains(publicUrl)){

            registerResult.setError(
                    String.format(
                            "The public tunnel %s is banned by the administrator.",
                            publicUrl
                    )
            );
            accessLogger.info(registerResult);
            readWriteLock.writeLock().unlock();
            return registerResult;

        }

        PublicTCPTunnel publicTCPTunnel =
                new PublicTCPTunnel(
                        controlConnection,
                        config,
                        random,
                        runtimeLogger,
                        accessLogger);

        result = publicTCPTunnel.bind(port);

        if (!"success".equalsIgnoreCase(result)){
            runtimeLogger.warn(result);
            registerResult.setError(result);
            return registerResult;
        }

        registerResult.setRemotePort(publicTCPTunnel.getPort());

        publicTCPTunnel.start();

        readWriteLock.writeLock().lock();

        if (tcpTunnels.get(publicUrl) != null){

            result = String.format("The public tunnel %s is already registered.", publicUrl);
            accessLogger.info(result);
            readWriteLock.writeLock().unlock();
            registerResult.setError(result);
            return registerResult;
        }

        tcpTunnels.put(publicUrl, publicTCPTunnel);
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

        registerResult.setState("success");
        return registerResult;
    }


    public String delete(String clientId) {

        readWriteLock.writeLock().lock();
        List<String> publicUrls = clientIdToPublicUrl.get(clientId);
        if (publicUrls == null){

            runtimeLogger.info(
                    String.format(
                            "Fail to get publicUrls of %s",
                            clientId
                    )
            );

            return "failure";
        }

        for (String publicUrl: publicUrls){
            PublicTCPTunnel publicTCPTunnel = tcpTunnels.remove(publicUrl);
            publicTCPTunnel.close();
        }

        runtimeLogger.info(
                String.format(
                        "Public tcpTunnels of %s closed",
                        clientId
                )
        );

        readWriteLock.writeLock().unlock();

        return "success";

    }



}

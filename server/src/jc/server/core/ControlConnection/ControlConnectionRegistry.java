package jc.server.core.ControlConnection;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ControlConnectionRegistry{

    private Logger runtimeLogger;
    private Logger accessLogger;

    private Map<String, ControlConnection> controlConnections = new HashMap<String, ControlConnection>();
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock(false);

    public ControlConnectionRegistry(Logger runtimeLogger, Logger accessLogger){
        this.runtimeLogger = runtimeLogger;
        this.accessLogger = accessLogger;
    }

    public void register(ControlConnection controlConnection) {

        String clientId = controlConnection.getClientId();

        readWriteLock.writeLock().lock();

        ControlConnection oldControlConnection = controlConnections.get(clientId);
        if (oldControlConnection != null){

            controlConnections.remove(clientId);

            try{
                oldControlConnection.close();
                accessLogger.info(
                        String.format(
                                "Close old control connection[%s] rom %s[%s]",
                                oldControlConnection.getClientId(),
                                oldControlConnection.getRemoteAddress(),
                                oldControlConnection.getClientId()
                        )
                );
            }catch (IOException e){
                accessLogger.info(
                        String.format("Fail to close old control connection[%s] from %s[%s]",
                                oldControlConnection.getConnectionId(),
                                oldControlConnection.getRemoteAddress(),
                                oldControlConnection.getClientId()
                        )
                );
            }

            accessLogger.info(String.format("close old control connection[%s] from %s",
                    oldControlConnection.getConnectionId(),
                    oldControlConnection.getRemoteAddress()));
        }

        controlConnections.put(clientId, controlConnection);
        accessLogger.info(String.format("Register control connection[%s] from %s[%s]",
                        controlConnection.getConnectionId(),
                        controlConnection.getRemoteAddress(),
                        controlConnection.getClientId())
        );
        readWriteLock.writeLock().unlock();

    }

    public ControlConnection get(String clientId) {

        readWriteLock.readLock().lock();
        ControlConnection controlConnection = controlConnections.get(clientId);
        readWriteLock.readLock().unlock();

        return controlConnection;
    }

    public void delete(String clientId){

        readWriteLock.writeLock().lock();
        controlConnections.remove(clientId);
        readWriteLock.writeLock().unlock();

    }

}

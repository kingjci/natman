package jc.server.core.ControlConnection;

import org.apache.log4j.Logger;

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
            oldControlConnection.close();

            accessLogger.info(String.format("close old control connection[%s] from %s",
                    oldControlConnection.getConnectionId(),
                    oldControlConnection.getRemoteAddress()));
        }

        controlConnections.put(clientId, controlConnection);
        accessLogger.info(String.format("Registered control connection %s[%s]", controlConnection.getRemoteAddress(),clientId));

        readWriteLock.writeLock().unlock();

    }

    public ControlConnection get(String clientId) {

        readWriteLock.readLock().lock();
        ControlConnection controlConnection = controlConnections.get(clientId);
        readWriteLock.readLock().unlock();

        return controlConnection;
    }

    public boolean has(String clientId){

        readWriteLock.readLock().lock();;
        ControlConnection controlConnection = controlConnections.get(clientId);
        readWriteLock.readLock().unlock();

        return !(controlConnection==null);

    }


    public void delete(String clientId){

        readWriteLock.writeLock().lock();
        controlConnections.remove(clientId);
        readWriteLock.writeLock().unlock();

    }

}

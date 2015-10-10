package jc.server.core.ControlConnection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import static jc.Utils.timeStamp;


/**
 * Created by ½ð³É on 2015/9/23.
 */
public class ControlConnectionRegistry{

    private Map<String, ControlConnection> controlConnections = new HashMap<String, ControlConnection>();
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock(false);

    public ControlConnectionRegistry(){

    }

    public void register(String clientId, ControlConnection controlConnection) {

        readWriteLock.writeLock().lock();

        ControlConnection old = controlConnections.get(clientId);
        if (old != null){
            controlConnections.remove(clientId);
            old.close();//shutdown
        }

        controlConnections.put(clientId, controlConnection);
        System.out.printf("[%s][ControlConnectionRegistry]Registered %s control connection with id %s\n", timeStamp(),controlConnection.getIp(),clientId);

        readWriteLock.writeLock().unlock();

    }

    public ControlConnection get(String clientId) {

        ControlConnection controlConnection = controlConnections.get(clientId);

        return controlConnection;
    }

    public int delete(String clientId) {
        return 0;
    }

    public boolean has(String clientId){

        readWriteLock.readLock().lock();

        ControlConnection controlConnection = controlConnections.get(clientId);

        readWriteLock.readLock().unlock();

        return controlConnection != null;
    }
}

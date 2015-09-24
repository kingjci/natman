package jc.server.core.registry;

import jc.server.core.control.ControlConnection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by ½ð³É on 2015/9/23.
 */
public class ControlConnectionRegistry implements Registry<ControlConnection> {

    private Map<String, ControlConnection> controlConnections = new HashMap<String, ControlConnection>();
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock(false);

    public ControlConnectionRegistry(){

    }


    @Override
    public int register(String clientId, ControlConnection controlConnection) {
        return 0;
    }

    @Override
    public ControlConnection get(String clientId) {
        return null;
    }

    @Override
    public int delete(String clientId) {
        return 0;
    }

    public boolean has(String clientId){

        readWriteLock.readLock().lock();

        ControlConnection controlConnection = controlConnections.get(clientId);

        return controlConnection != null;
    }
}

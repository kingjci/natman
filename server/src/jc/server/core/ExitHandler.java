package jc.server.core;

import org.apache.log4j.Logger;

/**
 * Created by 金成 on 2015/10/14.
 */
public class ExitHandler extends Thread {
    @Override
    public void run() {
        Logger runtimeLogger = Logger.getLogger("Runtime");

        runtimeLogger.debug("Goodbye!");
    }
}

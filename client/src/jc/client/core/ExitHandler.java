package jc.client.core;

import org.apache.log4j.Logger;

public class ExitHandler extends Thread {
    @Override
    public void run() {
        Logger runtimeLogger = Logger.getLogger("Runtime");

        runtimeLogger.debug("Goodbye!");
    }
}

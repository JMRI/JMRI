package jmri.jmrit.z21server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static java.lang.Thread.State.TERMINATED;

public class FacelessServer {

    private static FacelessServer instance;
    private static MainServer server;
    private static Thread currentThread;

    private final static Logger log = LoggerFactory.getLogger(FacelessServer.class);

    private FacelessServer() {
        server = new MainServer();
    }

    synchronized public static FacelessServer getInstance() {
        if (instance == null) {
            instance =  new FacelessServer();
        }
        return instance;
    }

    public void start() {
        if (currentThread == null || currentThread.getState() == TERMINATED) {
            log.info("Trying to start new z21 server...");
            currentThread = new Thread(server);
            currentThread.setName("Z21 App Server");
            currentThread.start();
        }
    }

    public void stop() {
        currentThread.interrupt();
    }


}

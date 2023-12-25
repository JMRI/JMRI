package jmri.jmrit.z21server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static java.lang.Thread.State.NEW;
import static java.lang.Thread.State.TERMINATED;

public class FacelessServer {

    private static FacelessServer instance;
    private static MainServer server;
    private static Thread currentThread;

    private final static Logger log = LoggerFactory.getLogger(FacelessServer.class);

    private FacelessServer() {
        initServer();
        createThread();
    }

    private static void initServer() {
        server = new MainServer();
    }

    private static synchronized void createThread() {
        currentThread = new Thread(server);
        currentThread.setName("Z21 App Server");
    }

    synchronized public static FacelessServer getInstance() {
        if (instance == null) {
            instance =  new FacelessServer();
        }
        return instance;
    }

    public synchronized void start() {
        if (currentThread.getState() == TERMINATED) {
            createThread();
        }
        if (currentThread.getState() == NEW) {
            log.info("Trying to start new z21 server...");
            currentThread.start();
        }
    }

    public synchronized void stop() {
        currentThread.interrupt();
    }


}

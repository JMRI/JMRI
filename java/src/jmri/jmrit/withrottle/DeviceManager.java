package jmri.jmrit.withrottle;

import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for WiThrottle device managers.
 *
 * @author Randall Wood Copyright 2011, 2017
 */
public interface DeviceManager extends RosterGroupSelector {

    public void listen();

    public default void createServerThread() {
        new DeviceManagerThread(this).start();
    }


    /**
     * Container for running {@link #listen() } in a separate thread.
     */
    public static class DeviceManagerThread extends Thread {

        DeviceManager manager;

        DeviceManagerThread(DeviceManager manager) {
            this.manager = manager;
            this.setName("WiThrottleServer"); // NOI18N
        }

        @Override
        public void run() {
            manager.listen();
            log.debug("Leaving DeviceManagerThread.run()");
        }

        private final static Logger log = LoggerFactory.getLogger(DeviceManagerThread.class);
    }
}

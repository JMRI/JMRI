package jmri.jmrix.loconet.locormi;

import jmri.jmrix.loconet.*;

/**
 * Client for the RMI LocoNet server.
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Bob Jacobsen
 * @version $Id: LnMessageClient.java,v 1.1 2002-03-28 04:21:19 jacobsen Exp $
 */

public class LnMessageClient extends LnTrafficRouter {

    public LnMessageClient() {
        super();
    }

    /**
     * Forward messages to the server.
     */
	public void sendLocoNetMessage(LocoNetMessage m) {
        // Implement this with code to forward m
        // to the server.
	}

    // messages that are received from the server should
    // be passed to this.notify(LocoNetMessage m);

    /**
     * Start the connection to the server. This is invoked
     * once.
     */
    void configureRemoteConnection(String remoteHostName, int timeoutSec) {
        if (log.isDebugEnabled()) log.debug("configureRemoteConnection: "
                                            +remoteHostName+" "+timeoutSec);
    }

    /**
	 * set up all of the other objects to operate with a server
	 * connected to this application
	 */
	public void configureLocalServices() {
			// This is invoked on the LnMessageClient after it's
            // ready to go, connection running, etc.

			// If a jmri.Programmer instance doesn't exist, create a
			// loconet.SlotManager to do that
			if (jmri.InstanceManager.programmerInstance() == null)
				jmri.jmrix.loconet.SlotManager.instance();

			// If a jmri.PowerManager instance doesn't exist, create a
			// loconet.LnPowerManager to do that
			if (jmri.InstanceManager.powerManagerInstance() == null)
				jmri.InstanceManager.setPowerManager(new jmri.jmrix.loconet.LnPowerManager());

			// If a jmri.TurnoutManager instance doesn't exist, create a
			// loconet.LnTurnoutManager to do that
			if (jmri.InstanceManager.turnoutManagerInstance() == null)
				jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.loconet.LnTurnoutManager());

            // the serial connections (LocoBuffer et al) start
            // various threads here.
	}


	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnMessageClient.class.getName());
}
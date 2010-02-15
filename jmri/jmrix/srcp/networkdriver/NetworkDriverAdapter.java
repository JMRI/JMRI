// NetworkDriverAdapter.java

package jmri.jmrix.srcp.networkdriver;

import jmri.jmrix.srcp.SRCPMessage;
import jmri.jmrix.srcp.SRCPPortController;
import jmri.jmrix.srcp.SRCPProgrammer;
import jmri.jmrix.srcp.SRCPProgrammerManager;
import jmri.jmrix.srcp.SRCPTrafficController;


/**
 * Implements SerialPortAdapter for the SRCP system network connection.
 * <P>This connects
 * an SRCP server (daemon) via a telnet connection.
 * Normally controlled by the NetworkDriverFrame class.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002, 2003, 2008, 2010
 * @author	Paul Bender Copyright (C) 2010
 * @version	$Revision: 1.7 $
 */
public class NetworkDriverAdapter extends SRCPPortController {

    /**
     * set up all of the other objects to operate with an SRCP command
     * station connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        SRCPTrafficController.instance().connectPort(this);

        jmri.InstanceManager.setProgrammerManager(
                new SRCPProgrammerManager(
                    new SRCPProgrammer()));

        jmri.InstanceManager.setPowerManager(new jmri.jmrix.srcp.SRCPPowerManager());

        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.srcp.SRCPTurnoutManager());

		jmri.InstanceManager.setThrottleManager(new jmri.jmrix.srcp.SRCPThrottleManager());

        // Create an instance of the consist manager.  Make sure this
        // happens AFTER the programmer manager to override the default   
        // consist manager.
        // jmri.InstanceManager.setConsistManager(new jmri.jmrix.srcp.SRCPConsistManager());

        jmri.InstanceManager.setCommandStation(new jmri.jmrix.srcp.SRCPCommandStation());

        // start the connection
        SRCPTrafficController.instance().sendSRCPMessage(new SRCPMessage("SET PROTOCOL SRCP 0.8.3\n"), null);
        SRCPTrafficController.instance().sendSRCPMessage(new SRCPMessage("SET CONNECTIONMODE SRCP COMMAND\n"), null);
        SRCPTrafficController.instance().sendSRCPMessage(new SRCPMessage("GO\n"), null);
        // mark OK for menus
        jmri.jmrix.srcp.ActiveFlag.setActive();
    }

    static public NetworkDriverAdapter instance() {
        if (mInstance == null) mInstance = new NetworkDriverAdapter();
        return mInstance;
    }
    static NetworkDriverAdapter mInstance = null;

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NetworkDriverAdapter.class.getName());

}

// NetworkDriverAdapter.java
package jmri.jmrix.srcp.networkdriver;

import jmri.jmrix.srcp.SRCPPortController;
import jmri.jmrix.srcp.SRCPTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*import java.io.*;
 import java.net.*;
 import java.util.Vector;*/
/**
 * Implements SerialPortAdapter for the SRCP system network connection.
 * <P>
 * This connects an SRCP server (daemon) via a telnet connection. Normally
 * controlled by the NetworkDriverFrame class.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2008
 * @author	Paul Bender Copyright (C) 2010
 * @version	$Revision$
 */
public class NetworkDriverAdapter extends SRCPPortController implements jmri.jmrix.NetworkPortAdapter {

    public NetworkDriverAdapter() {
        super(new jmri.jmrix.srcp.SRCPSystemConnectionMemo());
    }

    /**
     * set up all of the other objects to operate with an SRCP command station
     * connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        SRCPTrafficController control = new SRCPTrafficController();
        control.connectPort(this);
        this.getSystemConnectionMemo().setTrafficController(control);
        this.getSystemConnectionMemo().configureManagers();
        this.getSystemConnectionMemo().configureCommandStation();

        /*jmri.InstanceManager.setProgrammerManager(
         new SRCPProgrammerManager(
         new SRCPProgrammer()));

         jmri.InstanceManager.setPowerManager(new jmri.jmrix.srcp.SRCPPowerManager());

         jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.srcp.SRCPTurnoutManager());

         jmri.InstanceManager.setThrottleManager(new jmri.jmrix.srcp.SRCPThrottleManager());

         // Create an instance of the consist manager.  Make sure this
         // happens AFTER the programmer manager to override the default   
         // consist manager.
         // jmri.InstanceManager.setConsistManager(new jmri.jmrix.srcp.SRCPConsistManager());


         // mark OK for menus*/
        jmri.jmrix.srcp.ActiveFlag.setActive();
    }

    public boolean status() {
        return opened;
    }

    // private control members
    private boolean opened = false;

    static public NetworkDriverAdapter instance() {
        if (mInstance == null) {
            // create a new one
            NetworkDriverAdapter m = new NetworkDriverAdapter();
            m.setManufacturer(jmri.jmrix.DCCManufacturerList.ESU);

            // and make instance
            mInstance = m;
        }
        return mInstance;
    }
    static NetworkDriverAdapter mInstance = null;

    private final static Logger log = LoggerFactory.getLogger(NetworkDriverAdapter.class.getName());

}

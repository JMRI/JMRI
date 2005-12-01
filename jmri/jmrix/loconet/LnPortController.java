// LnPortController.java

package jmri.jmrix.loconet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Base for classes representing a LocoNet communications port
 * @author		Bob Jacobsen    Copyright (C) 2001, 2002
 * @version             $Revision: 1.9 $
 */
public abstract class LnPortController extends jmri.jmrix.AbstractPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to LnTrafficController classes, who in turn will deal in messages.

    // returns the InputStream from the port
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    public abstract DataOutputStream getOutputStream();

    /**
     * Check that this object is ready to operate. This is a question
     * of configuration, not transient hardware status.
     */
    public abstract boolean status();

    /**
     * Can the port accept additional characters?  This might
     * go false for short intervals, but it might also stick
     * off if something goes wrong.
     *<P>
     * Provide a default implementation for the MS100, etc,
     * in which this is _always_ true, as we rely on the
     * queueing in the port itself.
     */
    public boolean okToSend() {
        return true;
    }

    protected boolean mCanRead = true;
    protected boolean mProgPowersOff = false;

    protected String[] commandStationNames = {"DB150 (Empire Builder)",
                                    "DCS100 (Chief)", 
                                    "DCS200",
                                    "DCS50 (Zephyr)",
                                    "Intellibox"};
                                    
    /**
     * Set config info from the command station type name.
     */
    public void setCommandStationType(String value) {
		if (value == null) return;  // can happen while switching protocols
    	log.debug("setCommandStationType: "+value);
        if (value.equals("DB150 (Empire Builder)")) {
            mCanRead = false;
            mProgPowersOff = true;
        }
        else {
            mCanRead = true;
            mProgPowersOff = false;
        }
    }
                                    
    /**
     * Configure the programming manager and "command station" objects
     * @param mCanRead
     * @param mProgPowersOff
     */
    static public void configureCommandStation(boolean mCanRead, boolean mProgPowersOff) {
        // loconet.SlotManager to do programming (the Programmer instance is registered
        // when the SlotManager is created)
        jmri.jmrix.loconet.SlotManager.instance();
        // set slot manager's read capability
        jmri.jmrix.loconet.SlotManager.instance().setCanRead(mCanRead);
        jmri.jmrix.loconet.SlotManager.instance().setProgPowersOff(mProgPowersOff);
        // store as CommandStation object
        jmri.InstanceManager.setCommandStation(jmri.jmrix.loconet.SlotManager.instance());

    }

    /**
     * Configure the common managers for LocoNet connections.
     * This puts the common manager config in one
     * place.  This method is static so that it can be referenced
     * from classes that don't inherit, including hexfile.HexFileFrame
     * and locormi.LnMessageClient
     */
    static public void configureManagers() {
        jmri.InstanceManager.setPowerManager(new jmri.jmrix.loconet.LnPowerManager());

        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.loconet.LnTurnoutManager());

        jmri.InstanceManager.setSensorManager(new jmri.jmrix.loconet.LnSensorManager());

        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.loconet.LnThrottleManager());

        jmri.InstanceManager.setReporterManager(new jmri.jmrix.loconet.LnReporterManager());

    }
}


/* @(#)LnPortController.java */

// LnPortController.java

package jmri.jmrix.loconet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Base for classes representing a LocoNet communications port
 * @author		Bob Jacobsen    Copyright (C) 2001, 2002
 * @version             $Revision: 1.5 $
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
     */
    public abstract boolean okToSend();

    /**
     * Configure the programming manager and "command station" objects
     * @param mCanRead
     * @param mProgPowersOff
     */
    static public void configureCommandStation(boolean mCanRead, boolean mProgPowersOff) {
        // If a jmri.Programmer instance doesn't exist, create a
        // loconet.SlotManager to do that (the Programmer instance is registered
        // when the SlotManager is created)
        if (jmri.InstanceManager.programmerManagerInstance() == null)
            jmri.jmrix.loconet.SlotManager.instance();
        // set slot manager's read capability
        jmri.jmrix.loconet.SlotManager.instance().setCanRead(mCanRead);
        jmri.jmrix.loconet.SlotManager.instance().setProgPowersOff(mProgPowersOff);
        // store as CommandStation object
        if (jmri.InstanceManager.commandStationInstance() == null)
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
        // If a jmri.PowerManager instance doesn't exist, create a
        // loconet.LnPowerManager to do that
        if (jmri.InstanceManager.powerManagerInstance() == null)
            jmri.InstanceManager.setPowerManager(new jmri.jmrix.loconet.LnPowerManager());

        // If a jmri.TurnoutManager instance doesn't exist, create a
        // loconet.LnTurnoutManager to do that
        if (jmri.InstanceManager.turnoutManagerInstance() == null)
            jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.loconet.LnTurnoutManager());

        // If a jmri.SensorManager instance doesn't exist, create a
        // loconet.LnSensorManager to do that
        if (jmri.InstanceManager.sensorManagerInstance() == null)
            jmri.InstanceManager.setSensorManager(new jmri.jmrix.loconet.LnSensorManager());

        // If a jmri.ThrottleManager instance doesn't exist, create a
        // loconet.LnThrottleManager to do that
        if (jmri.InstanceManager.throttleManagerInstance() == null)
            jmri.InstanceManager.setThrottleManager(new jmri.jmrix.loconet.LnThrottleManager());

    }
}


/* @(#)LnPortController.java */

// SerialDriverAdapter.java

package jmri.jmrix.sprog.sprogCS;

import jmri.jmrix.sprog.SprogTrafficController;
import jmri.jmrix.sprog.SprogProgrammer;
import jmri.jmrix.sprog.SprogProgrammerManager;
import jmri.jmrix.sprog.SprogCommandStation;
import jmri.jmrix.sprog.SprogConstants.SprogMode;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;



/**
 * Implements SerialPortAdapter for the Sprog system.
 * <P>
 * This connects
 * an Sprog command station via a serial com port.
 * Also used for the USB SPROG, which appears to the computer as a
 * serial port.
 * Normally controlled by the SerialDriverFrame class.
 * <P>
 * The current implementation only handles the 9,600 baud rate, and does
 * not use any other options at configuration time.
 *
 * @author	Andrew Crosland   Copyright (C) 2006
 * @version	$Revision: 1.6 $
 */
public class SprogCSSerialDriverAdapter 
extends jmri.jmrix.sprog.serialdriver.SerialDriverAdapter {

    /**
     * set up all of the other objects to operate with an Sprog command
     * station connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        SprogTrafficController.instance().connectPort(this);

//        jmri.jmrix.sprog.SprogProgrammer.instance();  // create Programmer in InstanceManager
        jmri.InstanceManager.setProgrammerManager(new SprogProgrammerManager(new SprogProgrammer(), SprogMode.OPS));

        jmri.InstanceManager.setPowerManager(new jmri.jmrix.sprog.SprogPowerManager());

        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.sprog.SprogTurnoutManager());

//        jmri.InstanceManager.setCommandStation(new jmri.jmrix.sprog.SprogSoftCommandStation());
        // Start the command station queuing thread
        log.debug("start command station queuing thread");
        slotThread = new Thread(jmri.jmrix.sprog.SprogCommandStation.instance());
        slotThread.start();
        jmri.InstanceManager.setCommandStation(SprogCommandStation.instance());

        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.sprog.SprogCSThrottleManager());

        jmri.jmrix.sprog.ActiveFlagCS.setActive();

    }

    private Thread slotThread;

    static public SprogCSSerialDriverAdapter instance() {
        if (mInstance == null) mInstance = new SprogCSSerialDriverAdapter();
        return mInstance;
    }
    static SprogCSSerialDriverAdapter mInstance = null;
    
    String manufacturerName = jmri.jmrix.DCCManufacturerList.SPROG;
    
    public String getManufacturer() { return manufacturerName; }
    public void setManufacturer(String manu) { manufacturerName=manu; }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SprogCSSerialDriverAdapter.class.getName());

}

/* @(#)SerialdriverAdapter.java */

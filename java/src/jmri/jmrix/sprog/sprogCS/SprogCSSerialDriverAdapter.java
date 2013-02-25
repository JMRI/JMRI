// SerialDriverAdapter.java

package jmri.jmrix.sprog.sprogCS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.sprog.SprogTrafficController;
import jmri.jmrix.sprog.SprogConstants.SprogMode;




/**
 * Implements SerialPortAdapter for the Sprog system.
 * <P>
 * This connects
 * an Sprog command station via a serial com port.
 * Also used for the USB SPROG, which appears to the computer as a
 * serial port.
 * <P>
 * The current implementation only handles the 9,600 baud rate, and does
 * not use any other options at configuration time.
 *
 * @author	Andrew Crosland   Copyright (C) 2006
 * @version	$Revision$
 */
public class SprogCSSerialDriverAdapter 
extends jmri.jmrix.sprog.serialdriver.SerialDriverAdapter {

    public SprogCSSerialDriverAdapter() {
        super(SprogMode.OPS);
        //Set the username to match name, once refactored to handle multiple connections or user setable names/prefixes then this can be removed
        adaptermemo.setUserName("SPROG Command Station");
    }

    /**
     * set up all of the other objects to operate with an Sprog command
     * station connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        SprogTrafficController control = SprogTrafficController.instance();
        control.connectPort(this);

        adaptermemo.setSprogTrafficController(control);
        adaptermemo.configureCommandStation();
        adaptermemo.configureManagers();
        
        jmri.jmrix.sprog.ActiveFlagCS.setActive();

    }

    //private Thread slotThread;

    static public SprogCSSerialDriverAdapter instance() {
        if (mInstance == null) {
            SprogCSSerialDriverAdapter m = new SprogCSSerialDriverAdapter();
            m.setManufacturer(jmri.jmrix.DCCManufacturerList.SPROG);
            mInstance = m;
        }
        return mInstance;
    }
    static volatile SprogCSSerialDriverAdapter mInstance = null;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification="temporary until mult-system; only set when disposed")
    public void dispose(){
        if (adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
        mInstance = null;
    }
    
    static Logger log = LoggerFactory.getLogger(SprogCSSerialDriverAdapter.class.getName());

}

/* @(#)SerialdriverAdapter.java */

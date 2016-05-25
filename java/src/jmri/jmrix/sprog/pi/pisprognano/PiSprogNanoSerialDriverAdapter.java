// PiSprogNanoSerialDriverAdapter.java
package jmri.jmrix.sprog.pi.pisprognano;

import jmri.jmrix.sprog.SprogConstants.SprogMode;
import jmri.jmrix.sprog.SprogTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the Sprog system.
 * <P>
 * This connects an Pi-SPROG Nano via a serial com port or virtual USB serial 
 * com port.
 * <P>
 * The current implementation only handles the 115,200 baud rate, and does not use
 * any other options at configuration time.
 *
 * @author	Andrew Crosland Copyright (C) 2016
 * @version	$Revision$
 */
public class PiSprogNanoSerialDriverAdapter
        extends jmri.jmrix.sprog.serialdriver.SerialDriverAdapter {

    public PiSprogNanoSerialDriverAdapter() {
        super(SprogMode.OPS, 115200);
        options.put("TrackPowerState", new Option("Track Power At StartUp:", new String[]{"Powered Off", "Powered On"}, true));
        //Set the username to match name, once refactored to handle multiple connections or user setable names/prefixes then this can be removed
        this.getSystemConnectionMemo().setUserName("Pi-SPROG Nano Command Station");
    }

    /**
     * Get an array of valid baud rates. This is currently only 9,600 bps
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"115,200 bps"};
    }

    static public PiSprogNanoSerialDriverAdapter instance() {
        if (mInstance == null) {
            PiSprogNanoSerialDriverAdapter m = new PiSprogNanoSerialDriverAdapter();
            m.setManufacturer(jmri.jmrix.sprog.SprogConnectionTypeList.SPROG);
            mInstance = m;
        }
        return mInstance;
    }
    static volatile PiSprogNanoSerialDriverAdapter mInstance = null;

    static Logger log = LoggerFactory.getLogger(PiSprogNanoSerialDriverAdapter.class.getName());

}

/* @(#)PiSprogNanoSerialDriverAdapter.java */

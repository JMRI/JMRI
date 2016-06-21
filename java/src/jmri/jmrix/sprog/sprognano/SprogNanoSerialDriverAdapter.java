package jmri.jmrix.sprog.sprognano;

import jmri.jmrix.sprog.SprogConstants.SprogMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the Sprog system.
 * <P>
 * This connects an SSPROG DCC SPROG Nano command station via a USB virtual 
 * serial com port.
 * <P>
 * The current implementation only handles the 9,600 baud rate, and does not use
 * any other options at configuration time.
 *
 * @author	Andrew Crosland Copyright (C) 2016
 */
public class SprogNanoSerialDriverAdapter
        extends jmri.jmrix.sprog.serialdriver.SerialDriverAdapter {

    public SprogNanoSerialDriverAdapter() {
        super(SprogMode.OPS);
        options.put("TrackPowerState", new Option("Track Power At StartUp:", new String[]{"Powered Off", "Powered On"}, true));
        //Set the username to match name, once refactored to handle multiple connections or user setable names/prefixes then this can be removed
        this.getSystemConnectionMemo().setUserName("SPROG Nano Command Station");
    }

    static public SprogNanoSerialDriverAdapter instance() {
        if (mInstance == null) {
            SprogNanoSerialDriverAdapter m = new SprogNanoSerialDriverAdapter();
            m.setManufacturer(jmri.jmrix.sprog.SprogConnectionTypeList.SPROG);
            mInstance = m;
        }
        return mInstance;
    }
    static volatile SprogNanoSerialDriverAdapter mInstance = null;

    static Logger log = LoggerFactory.getLogger(SprogNanoSerialDriverAdapter.class.getName());

}

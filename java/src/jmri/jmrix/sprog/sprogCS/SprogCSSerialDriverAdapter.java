package jmri.jmrix.sprog.sprogCS;

import jmri.jmrix.sprog.SprogConstants.SprogMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the Sprog system.
 * <P>
 * This connects an Sprog command station via a serial com port. Also used for
 * the USB SPROG, which appears to the computer as a serial port.
 * <P>
 * The current implementation only handles the 9,600 baud rate, and does not use
 * any other options at configuration time.
 *
 * @author	Andrew Crosland Copyright (C) 2006
 */
public class SprogCSSerialDriverAdapter
        extends jmri.jmrix.sprog.serialdriver.SerialDriverAdapter {

    public SprogCSSerialDriverAdapter() {
        super(SprogMode.OPS);
        options.put("TrackPowerState", new Option("Track Power At StartUp:", new String[]{"Powered Off", "Powered On"}, true));
        //Set the username to match name, once refactored to handle multiple connections or user setable names/prefixes then this can be removed
        this.getSystemConnectionMemo().setUserName("SPROG Command Station");
    }

    static public SprogCSSerialDriverAdapter instance() {
        if (mInstance == null) {
            SprogCSSerialDriverAdapter m = new SprogCSSerialDriverAdapter();
            m.setManufacturer(jmri.jmrix.sprog.SprogConnectionTypeList.SPROG);
            mInstance = m;
        }
        return mInstance;
    }
    static volatile SprogCSSerialDriverAdapter mInstance = null;

    private final static Logger log = LoggerFactory.getLogger(SprogCSSerialDriverAdapter.class.getName());

}

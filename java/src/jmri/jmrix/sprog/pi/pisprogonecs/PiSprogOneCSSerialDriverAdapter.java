package jmri.jmrix.sprog.pi.pisprogonecs;

import jmri.jmrix.sprog.SprogConstants.SprogMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the Sprog system.
 * <P>
 * This connects a Pi-SPROG One command station via a serial com port.
 * <P>
 * The current implementation only handles the 115,200 baud rate, and does not use
 * any other options at configuration time.
 *
 * @author	Andrew Crosland Copyright (C) 2016
 */
public class PiSprogOneCSSerialDriverAdapter
        extends jmri.jmrix.sprog.serialdriver.SerialDriverAdapter {

    public PiSprogOneCSSerialDriverAdapter() {
        super(SprogMode.OPS, 115200);
        options.put("TrackPowerState", new Option("Track Power At StartUp:", new String[]{"Powered Off", "Powered On"}, true));
        //Set the username to match name, once refactored to handle multiple connections or user setable names/prefixes then this can be removed
        this.getSystemConnectionMemo().setUserName("Pi-SPROG One Command Station");
    }

    /**
     * Get an array of valid baud rates. This is currently only 115,200 bps
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"115,200 bps"};
    }

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public PiSprogOneCSSerialDriverAdapter instance() {
        return null;
    }

    static Logger log = LoggerFactory.getLogger(PiSprogOneCSSerialDriverAdapter.class.getName());

}

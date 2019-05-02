package jmri.jmrix.sprog.sprognano;

import jmri.jmrix.sprog.SprogConstants.SprogMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the Sprog system.
 * <p>
 * This connects an SSPROG DCC SPROG Nano command station via a USB virtual 
 * serial com port.
 * <p>
 * The current implementation only handles the 9,600 baud rate, and does not use
 * any other options at configuration time.
 *
 * @author	Andrew Crosland Copyright (C) 2016
 */
public class SprogNanoSerialDriverAdapter
        extends jmri.jmrix.sprog.serialdriver.SerialDriverAdapter {

    public SprogNanoSerialDriverAdapter() {
        super(SprogMode.OPS);
        options.put("TrackPowerState", new Option(Bundle.getMessage("OptionTrackPowerLabel"),
                new String[]{Bundle.getMessage("PowerStateOff"), Bundle.getMessage("PowerStateOn")},
                true)); // first element (TrackPowerState) NOI18N
        //Set the username to match name; once refactored to handle multiple connections or user setable names/prefixes then this can be removed
        this.getSystemConnectionMemo().setUserName(Bundle.getMessage("SprogNanoCSTitle"));
    }

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated  // will be removed when superclass method is removed due to @Override
    static public SprogNanoSerialDriverAdapter instance() {
        return null;
    }
    // private final static Logger log = LoggerFactory.getLogger(SprogNanoSerialDriverAdapter.class);

}

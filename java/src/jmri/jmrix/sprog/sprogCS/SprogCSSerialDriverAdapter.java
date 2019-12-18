package jmri.jmrix.sprog.sprogCS;

import jmri.jmrix.sprog.SprogConstants.SprogMode;

/**
 * Implement SerialPortAdapter for the Sprog system.
 * <p>
 * This connects an Sprog command station via a serial com port. Also used for
 * the USB SPROG, which appears to the computer as a serial port.
 * <p>
 * The current implementation only handles the 9,600 baud rate, and does not use
 * any other options at configuration time.
 *
 * @author	Andrew Crosland Copyright (C) 2006
 */
public class SprogCSSerialDriverAdapter
        extends jmri.jmrix.sprog.serialdriver.SerialDriverAdapter {

    public SprogCSSerialDriverAdapter() {
        super(SprogMode.OPS);
        options.put("TrackPowerState", new Option(Bundle.getMessage("OptionTrackPowerLabel"),
                new String[]{Bundle.getMessage("PowerStateOff"), Bundle.getMessage("PowerStateOn")},
                true)); // first element (TrackPowerState) NOI18N
        // Set the username to match name, once refactored to handle multiple connections or user setable names/prefixes then this can be removed
        this.getSystemConnectionMemo().setUserName(Bundle.getMessage("SprogCSTitle"));
    }

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated  // will be removed when class is converted
    static public SprogCSSerialDriverAdapter instance() {
        return null;
    }

    // private final static Logger log = LoggerFactory.getLogger(SprogCSSerialDriverAdapter.class);

}

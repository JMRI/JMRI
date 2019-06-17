package jmri.jmrix.sprog.pi.pisprogonecs;

import jmri.jmrix.sprog.SprogConstants.SprogMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the Sprog system.
 * <p>
 * This connects a Pi-SPROG One command station via a serial com port.
 * <p>
 * The current implementation only handles the 115,200 baud rate, and does not use
 * any other options at configuration time.
 *
 * @author	Andrew Crosland Copyright (C) 2016
 */
public class PiSprogOneCSSerialDriverAdapter
        extends jmri.jmrix.sprog.serialdriver.SerialDriverAdapter {

    public PiSprogOneCSSerialDriverAdapter() {
        super(SprogMode.OPS, 115200);
        options.put("TrackPowerState", new Option(Bundle.getMessage("OptionTrackPowerLabel"),
                new String[]{Bundle.getMessage("PowerStateOff"), Bundle.getMessage("PowerStateOn")},
                true)); // first element (TrackPowerState) NOI18N
        //Set the username to match name, once refactored to handle multiple connections or user setable names/prefixes then this can be removed
        this.getSystemConnectionMemo().setUserName(Bundle.getMessage("PiSprog1CSTitle"));
    }

    /**
     * {@inheritDoc}
     * Currently only 115,200 bps
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"115,200 bps"};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{115200};
    }

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated  // will be removed when class converted to multi-system
    static public PiSprogOneCSSerialDriverAdapter instance() {
        return null;
    }
    // private final static Logger log = LoggerFactory.getLogger(PiSprogOneCSSerialDriverAdapter.class);

}

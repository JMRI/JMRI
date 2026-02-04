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
 * @author Andrew Crosland Copyright (C) 2016
 */
public class SprogNanoSerialDriverAdapter
        extends jmri.jmrix.sprog.serialdriver.SerialDriverAdapter {

    public SprogNanoSerialDriverAdapter() {
        super(SprogMode.OPS);
        options.put("NumSlots", // NOI18N
                new Option(Bundle.getMessage("MakeLabel", Bundle.getMessage("NumSlotOptions")), // NOI18N
                        new String[]{"16", "8", "32", "48", "64"}, true));
        options.put("TrackPowerState", new Option(Bundle.getMessage("OptionTrackPowerLabel"),
                new String[]{Bundle.getMessage("PowerStateOff"), Bundle.getMessage("PowerStateOn")},
                true)); // first element (TrackPowerState) NOI18N
        //Set the username to match name; once refactored to handle multiple connections or user setable names/prefixes then this can be removed
        this.getSystemConnectionMemo().setUserName(Bundle.getMessage("SprogNanoCSTitle"));
    }
    
    /**
     * Set up all of the other objects to operate with an Sprog command station
     * connected to this port.
     */
    @Override
    public void configure() {
        String slots = getOptionState("NumSlots");
        try {
            numSlots = Integer.parseInt(slots);
        }
        catch (NumberFormatException e) {
            log.warn("Could not parse number of slots {}", e.getMessage());
            numSlots = 16;
        }
        
        super.configure();
    }

    private final static Logger log = LoggerFactory.getLogger(SprogNanoSerialDriverAdapter.class);

}

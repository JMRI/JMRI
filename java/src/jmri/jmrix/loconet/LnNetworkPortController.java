// LnNetworkPortController.java
package jmri.jmrix.loconet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for classes representing a LocoNet communications port
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 * @version $Revision: 1.24 $
 */
public abstract class LnNetworkPortController extends jmri.jmrix.AbstractNetworkPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to LnTrafficController classes, who in turn will deal in messages.

    private final static Logger log = LoggerFactory.getLogger(LnNetworkPortController.class);
    
    protected LnNetworkPortController(LocoNetSystemConnectionMemo connectionMemo) {
        super(connectionMemo);
        setManufacturer(jmri.jmrix.DCCManufacturerList.DIGITRAX);
    }

    protected LnCommandStationType commandStationType = null;

    protected boolean mTurnoutNoRetry = false;
    protected boolean mTurnoutExtraSpace = false;

    protected LnCommandStationType[] commandStationTypes = {
        LnCommandStationType.COMMAND_STATION_DCS100,
        LnCommandStationType.COMMAND_STATION_DCS200,
        LnCommandStationType.COMMAND_STATION_DCS050,
        LnCommandStationType.COMMAND_STATION_DCS051,
        LnCommandStationType.COMMAND_STATION_DB150,
        LnCommandStationType.COMMAND_STATION_IBX_TYPE_1,
        LnCommandStationType.COMMAND_STATION_IBX_TYPE_2,
        LnCommandStationType.COMMAND_STATION_LBPS,
        LnCommandStationType.COMMAND_STATION_MM};

    protected String[] commandStationNames;

    {
        commandStationNames = new String[commandStationTypes.length];
        int i = 0;
        for (LnCommandStationType type : commandStationTypes) {
            commandStationNames[i++] = type.getName();
        }
    }

    // There are also "PR3 standalone programmer" and "Stand-alone LocoNet"
    // in pr3/PR3Adapter
    /**
     * Set config info from a name, which needs to be one of the valid ones.
     */
    public void setCommandStationType(String name) {
        try {
            setCommandStationType(LnCommandStationType.getByName(name));
        } catch (IllegalArgumentException e) {
            // not a valid command station type, force
            log.error("Invalid command station name: \"{}\", defaulting to {}", name, commandStationTypes[0]);
            setCommandStationType(commandStationTypes[0]);
        }

    }

    /**
     * Set config info from the command station type enum.
     */
    public void setCommandStationType(LnCommandStationType value) {
        if (value == null) {
            return;  // can happen while switching protocols
        }
        log.debug("setCommandStationType: " + value);
        commandStationType = value;
    }

    @Override
    public LocoNetSystemConnectionMemo getSystemConnectionMemo() {
        return (LocoNetSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    public void setTurnoutHandling(String value) {
        if (value.equals("One Only") || value.equals("Both")) {
            mTurnoutNoRetry = true;
        }
        if (value.equals("Spread") || value.equals("Both")) {
            mTurnoutExtraSpace = true;
        }
        log.debug("turnout no retry: " + mTurnoutNoRetry);
        log.debug("turnout extra space: " + mTurnoutExtraSpace);
    }

    /**
     * Set the third port option. Only to be used after construction, but before
     * the openPort call
     */
    public void configureOption3(String value) {
        super.configureOption3(value);
        log.debug("configureOption3: " + value);
        setTurnoutHandling(value);
    }
}


/* @(#)LnNetworkPortController.java */

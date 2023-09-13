package jmri.jmrix.loconet;

import java.io.IOException;

import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.ShutDownTask;
import jmri.implementation.AbstractShutDownTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for classes representing a LocoNet communications port.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 * @author Bob Jacobsen    Copyright (C) 2023
 */
public abstract class LnNetworkPortController extends jmri.jmrix.AbstractNetworkPortController {

    /**
     * Base class. Implementations will provide InputStream and OutputStream
     * objects to LnTrafficController classes, who in turn will deal in messages.
     *
     * @param connectionMemo associated memo for this connection
     */
    protected LnNetworkPortController(LocoNetSystemConnectionMemo connectionMemo) {
        super(connectionMemo);
        setManufacturer(LnConnectionTypeList.DIGITRAX);
    }

    protected LnCommandStationType commandStationType = null;

    protected boolean mTurnoutNoRetry = false;
    protected boolean mTurnoutExtraSpace = false;
    protected boolean mInterrogateAtStart = true;

    protected boolean mTranspondingAvailable = false;

    protected boolean mLoconetProtocolAutoDetect = false;

    protected LnCommandStationType[] commandStationTypes = {
        LnCommandStationType.COMMAND_STATION_DCS100,
        LnCommandStationType.COMMAND_STATION_DCS240,
        LnCommandStationType.COMMAND_STATION_DCS210,
        LnCommandStationType.COMMAND_STATION_DCS200,
        LnCommandStationType.COMMAND_STATION_DCS050,
        LnCommandStationType.COMMAND_STATION_DCS051,
        LnCommandStationType.COMMAND_STATION_DCS052,
        LnCommandStationType.COMMAND_STATION_DB150,
        LnCommandStationType.COMMAND_STATION_IBX_TYPE_1,
        LnCommandStationType.COMMAND_STATION_IBX_TYPE_2,
        LnCommandStationType.COMMAND_STATION_LBPS,
        LnCommandStationType.COMMAND_STATION_MM,
        LnCommandStationType.COMMAND_STATION_DCS210PLUS,
        LnCommandStationType.COMMAND_STATION_DCS240PLUS};

    protected String[] commandStationNames;

    {
        commandStationNames = new String[commandStationTypes.length];
        int i = 0;
        for (LnCommandStationType type : commandStationTypes) {
            commandStationNames[i++] = type.getName();
        }
    }

    /**
     * While opening, also register a ShutDown item that
     * makes sure the socket is cleanly closed.
     */
    @Override
    public void connect() throws IOException {
        super.connect();
        
        ShutDownTask shutDownTask = new AbstractShutDownTask("Writing Blocks") {
            @Override
            public void run() {
                log.info("Closing LocoNet network connection");
                try {
                    socketConn.close();
                } catch (IOException ex) {
                    log.error("Exception closing LocoNet network connection", ex);
                }
            }
        };
        InstanceManager.getDefault(ShutDownManager.class).register(shutDownTask);
    }
    
    // There are also "PR3 standalone programmer" and "Stand-alone LocoNet"
    // in pr3/PR3Adapter
    /**
     * Set config info from a name, which needs to be one of the valid ones.
     *
     * @param name the name of the command station
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
     * Set configcommand station type.
     *
     * @param value command station type enum
     */
    public void setCommandStationType(LnCommandStationType value) {
        if (value == null) {
            return;  // can happen while switching protocols
        }
        log.debug("setCommandStationType: {}", value);
        commandStationType = value;
    }

    @Override
    public LocoNetSystemConnectionMemo getSystemConnectionMemo() {
        return (LocoNetSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    public void setTurnoutHandling(String value) {
        if (value.equals("One Only") || value.equals(Bundle.getMessage("HandleOneOnly"))
                || value.equals("Both") || value.equals(Bundle.getMessage("HandleBoth"))) {
            mTurnoutNoRetry = true;
        }
        log.debug("turnout no retry: {}", mTurnoutNoRetry); // NOI18N
        if (value.equals("Spread") || value.equals(Bundle.getMessage("HandleSpread"))
                || value.equals("Both") || value.equals(Bundle.getMessage("HandleBoth"))) {
            mTurnoutExtraSpace = true;
        }
        log.debug("turnout extra space: {}", mTurnoutExtraSpace); // NOI18N
    }

    /**
     * Set whether transponding is available.
     *
     * @param value either yes or no
     */
    public void setTranspondingAvailable(String value) {
        // default (most common state) is off, so just check for Yes
        mTranspondingAvailable = (value.equals("Yes") || value.equals(Bundle.getMessage("ButtonYes")));
        log.debug("transponding available: {}", mTranspondingAvailable); // NOI18N
    }

    /**
     * Set whether to use XP slots if available or not.
     *
     * @param value either Bundle.getMessage("LoconetProtocolAutoDetect") or no
     */
    public void setLoconetProtocolAutoDetect(String value) {
        // default (most common state) is off, so just check for Yes
        mLoconetProtocolAutoDetect = (value.equals("Yes") || value.equals(Bundle.getMessage("LoconetProtocolAutoDetect")));
        log.debug("Loconet XPSlots: {}", mLoconetProtocolAutoDetect); // NOI18N
     }
    /**
     * Set whether to interrogate at startup
     *
     * @param value either yes or no
     */
    public void setInterrogateOnStart(String value) {
        // default (most common state) is on, so just check for No
        mInterrogateAtStart = !(value.equals("No") || value.equals(Bundle.getMessage("ButtonNo")));
        log.debug("Interrogate at StartUp: {}", mInterrogateAtStart); // NOI18N
    }

    /**
     * Set the third port option. Only to be used after construction, but before
     * the openPort call.
     */
    @Override
    public void configureOption3(String value) {
        super.configureOption3(value);
        log.debug("configureOption3: {}", value);
        setTurnoutHandling(value);
    }

    private final static Logger log = LoggerFactory.getLogger(LnNetworkPortController.class);

}

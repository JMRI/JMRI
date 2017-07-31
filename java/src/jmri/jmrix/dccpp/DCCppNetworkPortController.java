package jmri.jmrix.dccpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for classes representing a LocoNet communications port
 *
 * @author Kevin Dickerson Copyright (C) 2011
 * @author Mark Underwoodn Copyright (C) 2015
 *
 * Based o LnNetworkPortController by Kevin Dickerson
 */
public abstract class DCCppNetworkPortController extends jmri.jmrix.AbstractNetworkPortController implements DCCppPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to LnTrafficController classes, who in turn will deal in messages.

    private final static Logger log = LoggerFactory.getLogger(DCCppNetworkPortController.class);
    
    protected DCCppNetworkPortController() {
        super(new DCCppSystemConnectionMemo());
        setManufacturer(DCCppConnectionTypeList.DCCPP);
        allowConnectionRecovery = true;
    }
    
    protected DCCppNetworkPortController(DCCppSystemConnectionMemo connectionMemo) {
        super(connectionMemo);
        setManufacturer(DCCppConnectionTypeList.DCCPP);
    }

    protected int commandStationType = 0;

    protected boolean mTurnoutNoRetry = false;
    protected boolean mTurnoutExtraSpace = false;

    protected int[] commandStationTypes = {
        DCCppConstants.DCCPP_UNO_1_0,
        DCCppConstants.DCCPP_ARDUINO_1_1
    };
    
    protected String[] commandStationNames;
    
    {
        commandStationNames = new String[commandStationTypes.length];
        for (int i = 0; i < commandStationTypes.length; i++) {
            commandStationNames[i] = DCCppConstants.CommandStationNames[i];
        }
    }
    
    // There are also "PR3 standalone programmer" and "Stand-alone LocoNet"
    // in pr3/PR3Adapter
    /**
     * Set config info from a name, which needs to be one of the valid ones.
     */
    public void setCommandStationType(String name) {
        for (int i = 0; i < commandStationNames.length; i++) {
            if (commandStationNames[i].matches(name)) {
                commandStationType = i;
                return;
            }
        }
        log.error("CommandStation Type not found: {}", name);
        commandStationType = 0;
    }
    
    /**
     * Set config info from the command station type enum.
     */
    public void setCommandStationType(int value) {
        log.debug("setCommandStationType: {}" + Integer.toString(value));
        commandStationType = value;
    }
    
    @Override
    public DCCppSystemConnectionMemo getSystemConnectionMemo() {
        return (DCCppSystemConnectionMemo) super.getSystemConnectionMemo();
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
    @Override
    public void configureOption3(String value) {
        super.configureOption3(value);
        log.debug("configureOption3: " + value);
        setTurnoutHandling(value);
    }


     /**
     * Check that this object is ready to operate. This is a question of
     * configuration, not transient hardware status.
     */
    @Override
    public abstract boolean status();

    /**
     * Can the port accept additional characters? This might go false for short
     * intervals, but it might also stick off if something goes wrong.
     */
    @Override
    public abstract boolean okToSend();

    @Override
    public void setOutputBufferEmpty(boolean s) {
    } // Maintained for compatibility with DCCpptPortController. Simply ignore calls !!!

    /**
     * Customizable method to deal with resetting a system connection after a
     * successful recovery of a connection.
     */
    @Override
    protected void resetupConnection() {
        this.getSystemConnectionMemo().getDCCppTrafficController().connectPort(this);
    }

}




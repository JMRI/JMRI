// DCCppNetworkPortController.java
package jmri.jmrix.dccpp;

/**
 * Base for classes representing a LocoNet communications port
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 * @author	Mark Underwoodn Copyright (C) 2015
 * @version $Revision: 1.24 $
 *
 * Based o LnNetworkPortController by Kevin Dickerson
 */
public abstract class DCCppNetworkPortController extends jmri.jmrix.AbstractNetworkPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to LnTrafficController classes, who in turn will deal in messages.

    protected DCCppNetworkPortController(DCCppSystemConnectionMemo connectionMemo) {
        super(connectionMemo);
        setManufacturer(jmri.jmrix.DCCManufacturerList.DCCPP);
    }

    protected int commandStationType = 0;

    protected boolean mTurnoutNoRetry = false;
    protected boolean mTurnoutExtraSpace = false;

    protected int[] commandStationTypes = {
	DCCppConstants.DCCPP_UNO_1_5
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
    public void configureOption3(String value) {
        super.configureOption3(value);
        log.debug("configureOption3: " + value);
        setTurnoutHandling(value);
    }
}


/* @(#)DCCppNetworkPortController.java */

// LnPortController.java

package jmri.jmrix.loconet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Base for classes representing a LocoNet communications port
 * @author		Bob Jacobsen    Copyright (C) 2001, 2002
 * @version             $Revision$
 */
public abstract class LnPortController extends jmri.jmrix.AbstractSerialPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to LnTrafficController classes, who in turn will deal in messages.

    public LnPortController(){
        super();
        setManufacturer(jmri.jmrix.DCCManufacturerList.DIGITRAX);
    }

    // returns the InputStream from the port
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    public abstract DataOutputStream getOutputStream();

    /**
     * Check that this object is ready to operate. This is a question
     * of configuration, not transient hardware status.
     */
    public abstract boolean status();

    /**
     * Can the port accept additional characters?  This might
     * go false for short intervals, but it might also stick
     * off if something goes wrong.
     *<P>
     * Provide a default implementation for the MS100, etc,
     * in which this is _always_ true, as we rely on the
     * queueing in the port itself.
     */
    public boolean okToSend() {
        return true;
    }

    protected LocoNetSystemConnectionMemo adaptermemo = null;

    protected boolean mCanRead = true;
    protected boolean mProgPowersOff = false;
    protected String commandStationName = "<unknown>";
    protected boolean mTurnoutNoRetry = false;
    protected boolean mTurnoutExtraSpace = false;
    
    protected String[] commandStationNames = {
                                    "DCS100 (Chief)", 
                                    "DCS200",
                                    "DCS50 (Zephyr)",
                                    "DCS51 (Zephyr Xtra)",
                                    "DB150 (Empire Builder)",
                                    "Intellibox",
                                    "LocoBuffer (PS)",
                                    "Mix-Master"};
    
    // There are also "PR3 standalone programmer" and "Stand-alone LocoNet"
    // in pr3/PR3Adapter
                                    
    /**
     * Set config info from the command station type name.
     */
    public void setCommandStationType(String value) {
		if (value == null) return;  // can happen while switching protocols
    	log.debug("setCommandStationType: "+value);
        if (value.equals("DB150 (Empire Builder)") || value.equals("Mix-Master")) {
            mCanRead = false;
            mProgPowersOff = true;
        }
        else {
            mCanRead = true;
            mProgPowersOff = false;
        }
        commandStationName = value;
    }
    public void setTurnoutHandling(String value) {
        if (value.equals("One Only") || value.equals("Both")) mTurnoutNoRetry = true;
        if (value.equals("Spread") || value.equals("Both")) mTurnoutExtraSpace = true;
        log.debug("turnout no retry: "+mTurnoutNoRetry);
        log.debug("turnout extra space: "+mTurnoutExtraSpace);
    }
    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }
    static Logger log = LoggerFactory.getLogger(LnPortController.class.getName());
}


/* @(#)LnPortController.java */

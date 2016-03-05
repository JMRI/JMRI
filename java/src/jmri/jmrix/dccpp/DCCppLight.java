// DCCppLight.java
package jmri.jmrix.dccpp;

import jmri.implementation.AbstractLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DCCppLight.java
 *
 * Implementation of the Light Object for DCC++ NOTE: This is a
 * simplification of the DCCppTurnout class.
 * <P>
 * Based in part on SerialLight.java
 *
 * @author Paul Bender Copyright (C) 2008-2010
 * @author Mark Underwood Copyright (C) 2015
 * @version $Revision$
 */
public class DCCppLight extends AbstractLight implements DCCppListener {

    private DCCppTrafficController tc = null;
    private DCCppLightManager lm = null;

    /**
     * Create a Light object, with only system name.
     * <P>
     * 'systemName' was previously validated in DCCppLightManager
     */
    public DCCppLight(DCCppTrafficController tc, DCCppLightManager lm, String systemName) {
        super(systemName);
        this.tc = tc;
        this.lm = lm;
        // Initialize the Light
        initializeLight(systemName);
    }

    /**
     * Create a Light object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in DCCppLightManager
     */
    public DCCppLight(DCCppTrafficController tc, DCCppLightManager lm, String systemName, String userName) {
        super(systemName, userName);
        this.tc = tc;
        this.lm = lm;
        // Initialize the Light
        initializeLight(systemName);
    }

    /*
     * Dispose of the light object
     */
    public void dispose() {
        tc.removeDCCppListener(DCCppInterface.FEEDBACK | DCCppInterface.COMMINFO | DCCppInterface.CS_INFO, this);
        super.dispose();
    }

    /*
     *  Initilize the light object's parameters
     */
    private synchronized void initializeLight(String systemName) {
        // Save system name
        mSystemName = systemName;
        // Extract the Bit from the name
        mAddress = lm.getBitFromSystemName(systemName);
        // Set initial state
        setState(OFF);
        // At construction, register for messages
        tc.addDCCppListener(DCCppInterface.FEEDBACK | DCCppInterface.COMMINFO | DCCppInterface.CS_INFO, this);

    }

    /**
     * Sets up system dependent instance variables and sets system independent
     * instance variables to default values Note: most instance variables are in
     * AbstractLight.java
     */
    /**
     * System dependent instance variables
     */
    String mSystemName = "";     // system name 
    //protected int mState = OFF;  // current state of this light
    //private int mOldState =mState; // save the old state
    int mAddress = 0;            // accessory output address

    /* Internal State Machine states. */
    static final int OFFSENT = 1;
    static final int COMMANDSENT = 2;
    static final int IDLE = 0;
    //private int InternalState = IDLE;

    /**
     * Return the current state of this Light
     */
    synchronized public int getState() {
        return mState;
    }

    /**
     * Set the current state of this Light This routine requests the hardware to
     * change.
     */
    synchronized public void setState(int newState) {
        if (newState != ON && newState != OFF) {
            // Unsuported state
            log.warn("Unsupported state " + newState + " requested for light " + mSystemName);
            return;
        }

	log.debug("Light Set State: mstate = {} newstate = {}", mState, newState);

        // get the right packet
	// Going to use the Stationary Decoder here, with the convention/assumption that:
	// mAddress = (address - 1) * 4 + subaddress + 1 for address>0;
	if (mAddress > 0) {
	    int addr = (mAddress - 1) / (DCCppConstants.MAX_ACC_DECODER_SUBADDR + 1);
	    int subaddr = (mAddress - 1) % (DCCppConstants.MAX_ACC_DECODER_SUBADDR + 1);
	    boolean state = (newState == jmri.Light.ON);
	    DCCppMessage msg = DCCppMessage.makeAccessoryDecoderMsg(addr,
								    subaddr,
								    state);
	    //InternalState = COMMANDSENT;
	    tc.sendDCCppMessage(msg, this);

	    if (newState != mState) {
		int oldState = mState;
		mState = newState;
		// notify listeners, if any
		firePropertyChange("KnownState", Integer.valueOf(oldState), Integer.valueOf(newState));
	    }
	}
    }

    /*
     *  Handle an incoming message from the DCC++ Base Station
     *  NOTE: We aren't registered as a listener, so This is only triggered 
     *  when we send out a message
     */
    synchronized public void message(DCCppReply l) {
        if (log.isDebugEnabled()) {
            log.debug("recieved message: " + l);
        }
	// We don't expect a reply, so we don't do anything with replies.
    }

    // listen for the messages to the LI100/LI101
    public void message(DCCppMessage l) {
    }

    // Handle a timeout notification
    public void notifyTimeout(DCCppMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppLight.class.getName());
}

/* @(#)DCCppLight.java */

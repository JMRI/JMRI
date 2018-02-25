package jmri.jmrix.dccpp;

import jmri.implementation.AbstractLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Light Object for DCC++
 * <p>
 * NOTE: This is a simplification of the DCCppTurnout class.
 * <p>
 * Based in part on SerialLight.java
 *
 * @author Paul Bender Copyright (C) 2008-2010
 * @author Mark Underwood Copyright (C) 2015
 */
public class DCCppLight extends AbstractLight implements DCCppListener {

    private DCCppTrafficController tc = null;
    private DCCppLightManager lm = null;

    /**
     * Create a Light object, with only system name.
     * <p>
     * 'systemName' was previously validated in DCCppLightManager
     *
     * @param tc         the traffic controller for the connection
     * @param lm         the managing LightManager for this Light
     * @param systemName the system name for this Light
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
     * <p>
     * 'systemName' was previously validated in DCCppLightManager
     *
     * @param tc         the traffic controller for the connection
     * @param lm         the managing LightManager for this Light
     * @param systemName the system name for this Light
     * @param userName   the user name for this Light
     */
    public DCCppLight(DCCppTrafficController tc, DCCppLightManager lm, String systemName, String userName) {
        super(systemName, userName);
        this.tc = tc;
        this.lm = lm;
        // Initialize the Light
        initializeLight(systemName);
    }

    /**
     * Dispose of the light object.
     */
    @Override
    public void dispose() {
        tc.removeDCCppListener(DCCppInterface.FEEDBACK | DCCppInterface.COMMINFO | DCCppInterface.CS_INFO, this);
        super.dispose();
    }

    /**
     * Initialize the light object's parameters.
     */
    private synchronized void initializeLight(String systemName) {
        // Extract the Bit from the name
        mAddress = lm.getBitFromSystemName(systemName);
        // Set initial state
        setState(OFF);
        // At construction, register for messages
        tc.addDCCppListener(DCCppInterface.FEEDBACK | DCCppInterface.COMMINFO | DCCppInterface.CS_INFO, this);
    }

    /**
     * Sets up system dependent instance variables and set system independent
     * instance variables to default values.
     * <p>
     * Note: most instance variables are in AbstractLight.java
     */

    /**
     * System dependent instance variables
     */
    //protected int mState = OFF;  // current state of this light
    //private int mOldState =mState; // save the old state
    int mAddress = 0;            // accessory output address

    /* Internal State Machine states. */
    static final int OFFSENT = 1;
    static final int COMMANDSENT = 2;
    static final int IDLE = 0;
    //private int InternalState = IDLE;

    /**
     * Set the current state of this Light.
     * This routine requests the hardware to change.
     */
    @Override
    synchronized public void setState(int newState) {
        if (newState != ON && newState != OFF) {
            // Unsuported state
            log.warn("Unsupported state {} requested for light {}", newState, getSystemName());
            return;
        }

        log.debug("Light Set State: mstate = {} newstate = {}", mState, newState);

        // get the right packet
        if (mAddress > 0) {
            boolean state = (newState == jmri.Light.ON);
            DCCppMessage msg = DCCppMessage.makeAccessoryDecoderMsg(mAddress, state);
            //InternalState = COMMANDSENT;
            tc.sendDCCppMessage(msg, this);

            if (newState != mState) {
                int oldState = mState;
                mState = newState;
                // notify listeners, if any
                firePropertyChange("KnownState", oldState, newState);
            }
        }
    }

    /**
     * {@inheritDoc}
     * Handle an incoming message from the DCC++ Base Station.
     * <p>
     * NOTE: We aren't registered as a listener, so this is only triggered
     * when we send out a message
     */
    @Override
    synchronized public void message(DCCppReply l) {
        log.debug("received message: {}", l);
        // We don't expect a reply, so we don't do anything with replies.
    }

    /**
     * {@inheritDoc}
     * Listen for messages to the DCC++ Base Station.
     */
    @Override
    public void message(DCCppMessage l) {
        // messages not handled by DCCpp lights
    }

    // Handle a timeout notification
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message {}", msg.toString());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppLight.class);

}

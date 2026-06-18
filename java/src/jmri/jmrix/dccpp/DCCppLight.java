package jmri.jmrix.dccpp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.implementation.AbstractLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Light Object for DCC-EX
 * <p>
 * NOTE: This is a simplification of the DCCppTurnout class.
 * <p>
 * Based in part on SerialLight.java
 *
 * @author Paul Bender Copyright (C) 2008-2010
 * @author Mark Underwood Copyright (C) 2015
 */
public class DCCppLight extends AbstractLight implements DCCppListener {

    @SuppressFBWarnings(value = "MS_PKGPROTECT",
            justification = "Public for access by manager, tests, and SelectionPropertyDescriptor")
    public static final String[] MODE_NAMES = {"Accessory Decoder", "CS VPIN"}; // NOI18N

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
        mAddress = lm.getBitFromSystemName(systemName);
        setState(OFF);
        tc.addDCCppListener(DCCppInterface.FEEDBACK | DCCppInterface.COMMINFO | DCCppInterface.CS_INFO, this);
    }

    int mAddress = 0;            // accessory output address

    /* Internal State Machine states. */
    static final int OFFSENT = 1;
    static final int COMMANDSENT = 2;
    static final int IDLE = 0;

    /**
     * Set the current state of this Light.
     * This routine requests the hardware to change.
     */
    @Override
    synchronized public void setState(int newState) {
        if (newState != ON && newState != OFF) {
            log.warn("Unsupported state {} requested for light {}", newState, getSystemName());
            return;
        }

        log.debug("Light Set State: mstate = {} newstate = {}", mState, newState);

        if (mAddress > 0) {
            boolean state = (newState == jmri.Light.ON);
            DCCppMessage msg;
            if (MODE_NAMES[1].equals(getProperty(DCCppLightManager.DCCPP_LIGHT_MODE_KEY))) {
                // CS VPIN: HIGH = ON, LOW = OFF
                msg = DCCppMessage.makeOutputCmdMsgLC(mAddress, state);
            } else {
                msg = DCCppMessage.makeAccessoryDecoderMsg(mAddress, state);
            }
            tc.sendDCCppMessage(msg, this);

            if (newState != mState) {
                int oldState = mState;
                mState = newState;
                firePropertyChange("KnownState", oldState, newState);
            }
        }
    }

    /**
     * {@inheritDoc}
     * Handle an incoming message from the DCC-EX Base Station.
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
     * Listen for messages to the DCC-EX Base Station.
     */
    @Override
    public void message(DCCppMessage l) {
        // messages not handled by DCCpp lights
    }

    // Handle a timeout notification
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        log.debug("Notified of timeout on message '{}'", msg);
    }

    private static final Logger log = LoggerFactory.getLogger(DCCppLight.class);

}

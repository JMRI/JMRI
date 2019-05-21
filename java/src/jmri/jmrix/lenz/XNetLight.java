package jmri.jmrix.lenz;

import jmri.implementation.AbstractLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Light Object for XpressNet.
 * <p>
 * NOTE: This is a simplification of the XNetTurnout class.
 * <p>
 * Based in part on SerialLight.java
 *
 * @author Paul Bender Copyright (C) 2008-2010
 */
public class XNetLight extends AbstractLight implements XNetListener {

    private XNetTrafficController tc = null;
    private XNetLightManager lm = null;

    /**
     * Create a Light object, with only system name.
     * <p>
     * 'systemName' was previously validated in LnLightManager
     *
     * @param tc         the traffic controller for the connection
     * @param lm         the managing LightManager for this Light
     * @param systemName the system name for this Light
     */
    public XNetLight(XNetTrafficController tc, XNetLightManager lm, String systemName) {
        super(systemName);
        this.tc = tc;
        this.lm = lm;
        // Initialize the Light
        initializeLight(systemName);
    }

    /**
     * Create a Light object, with both system and user names.
     * <p>
     * 'systemName' was previously validated in XNetLightManager
     *
     * @param tc         the traffic controller for the connection
     * @param lm         the managing LightManager for this Light
     * @param systemName the system name for this Light
     * @param userName   the user name for this Light
     */
    public XNetLight(XNetTrafficController tc, XNetLightManager lm, String systemName, String userName) {
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
        tc.removeXNetListener(XNetInterface.FEEDBACK | XNetInterface.COMMINFO | XNetInterface.CS_INFO, this);
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
        tc.addXNetListener(XNetInterface.FEEDBACK | XNetInterface.COMMINFO | XNetInterface.CS_INFO, this);
    }

    /*
     * Set up system dependent instance variables and set system independent
     * instance variables to default values.
     * Note: most instance variables are in AbstractLight.java
     */

    /**
     * System dependent instance variables
     */
    int mAddress = 0;            // accessory output address

    /**
     * Internal State Machine states.
     */
    static final int OFFSENT = 1;
    static final int COMMANDSENT = 2;
    static final int IDLE = 0;
    private int internalState = IDLE;

    /**
     * Set the current state of this Light. This routine requests the hardware
     * to change.
     */
    @Override
    synchronized public void setState(int newState) {
        if (newState != ON && newState != OFF) {
            // Unsuported state
            log.warn("Unsupported state {} requested for light {}", newState, mSystemName);
            return;
        }

        // get the right packet
        XNetMessage msg = XNetMessage.getTurnoutCommandMsg(mAddress,
                newState == ON,
                newState == OFF,
                true);
        internalState = COMMANDSENT;
        tc.sendXNetMessage(msg, this);

        if (newState != mState) {
            int oldState = mState;
            mState = newState;
            // notify listeners, if any
            firePropertyChange("KnownState", oldState, newState);
        }
        sendOffMessage();
    }

    /**
     * Handle an incoming message from the XpressNet. NOTE: We aren't registered
     * as a listener, so this is only triggered when we send out a message.
     *
     * @param l the message to handle
     */
    @Override
    synchronized public void message(XNetReply l) {
        if (log.isDebugEnabled()) {
            log.debug("received message: {}", l);
        }
        if (internalState == OFFSENT) {
            // If an OFF was sent, we want to check for Communications
            // errors before we try to do anything else.
            if (l.isCommErrorMessage()) {
                /* this is a communications error */
                log.error("Communications error occurred - message received was: {}", l);
                sendOffMessage();
            } else if (l.isCSBusyMessage()) {
                /* this is a communications error */
                log.error("Command station busy - message received was: {}", l);
                sendOffMessage();
            } else if (l.isOkMessage()) {
                /* the command was successfully received */
                internalState = IDLE;
            } else if (internalState == COMMANDSENT) {
                // If command was sent,, we want to check for Communications
                // errors before we try to do anything else.
                if (l.isCommErrorMessage()) {
                    /* this is a communications error */
                    log.error("Communications error occurred - message received was: {}", l);
                    setState(mState);
                    return;
                } else if (l.isCSBusyMessage()) {
                    /* this is a communications error */
                    log.error("Command station busy - message received was: {}", l);
                    setState(mState);
                    return;
                } else if (l.isOkMessage()) {
                    /* the command was successfully received */
                    sendOffMessage();
                }
            }
        }
    }

    /**
     * Listen for the messages to the LI100/LI101.
     *
     * @param l the expected message
     */
    @Override
    public void message(XNetMessage l) {
    }

    // Handle a timeout notification
    @Override
    public void notifyTimeout(XNetMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    /**
     * Send an "Off" message to the decoder for this output.
     */
    private synchronized void sendOffMessage() {
        // We need to tell the turnout to shut off the output.
        if (log.isDebugEnabled()) {
            log.debug("Sending off message for light " + mAddress + " commanded state= " + mState);
        }
        XNetMessage msg = XNetMessage.getTurnoutCommandMsg(mAddress,
                mState == ON,
                mState == OFF,
                false);
        tc.sendXNetMessage(msg, this);

        // Set the known state to the commanded state.
        internalState = OFFSENT;
    }

    private final static Logger log = LoggerFactory.getLogger(XNetLight.class);

}

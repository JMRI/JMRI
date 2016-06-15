// XNetLight.java
package jmri.jmrix.lenz;

import jmri.implementation.AbstractLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XNetLight.java
 *
 * Implementation of the Light Object for XPressNet NOTE: This is a
 * simplification of the XNetTurnout class.
 * <P>
 * Based in part on SerialLight.java
 *
 * @author Paul Bender Copyright (C) 2008-2010
 * @version $Revision$
 */
public class XNetLight extends AbstractLight implements XNetListener {

    /**
     *
     */
    private static final long serialVersionUID = -1847924231251447075L;
    private XNetTrafficController tc = null;
    private XNetLightManager lm = null;

    /**
     * Create a Light object, with only system name.
     * <P>
     * 'systemName' was previously validated in LnLightManager
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
     * <P>
     * 'systemName' was previously validated in XNetLightManager
     */
    public XNetLight(XNetTrafficController tc, XNetLightManager lm, String systemName, String userName) {
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
        tc.removeXNetListener(XNetInterface.FEEDBACK | XNetInterface.COMMINFO | XNetInterface.CS_INFO, this);
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
        tc.addXNetListener(XNetInterface.FEEDBACK | XNetInterface.COMMINFO | XNetInterface.CS_INFO, this);

    }

    /**
     * Sets up system dependent instance variables and sets system independent
     * instance variables to default values Note: most instance variables are in
     * AbstractLight.java
     */
    /**
     * System dependent instance variables
     */
    int mAddress = 0;            // accessory output address

    /* Internal State Machine states. */
    static final int OFFSENT = 1;
    static final int COMMANDSENT = 2;
    static final int IDLE = 0;
    private int InternalState = IDLE;

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

        // get the right packet
        XNetMessage msg = XNetMessage.getTurnoutCommandMsg(mAddress,
                newState == ON,
                newState == OFF,
                true);
        InternalState = COMMANDSENT;
        tc.sendXNetMessage(msg, this);

        if (newState != mState) {
            int oldState = mState;
            mState = newState;
            // notify listeners, if any
            firePropertyChange("KnownState", Integer.valueOf(oldState), Integer.valueOf(newState));
        }
        sendOffMessage();
    }

    /*
     *  Handle an incoming message from the XPressNet
     *  NOTE: We aren't registered as a listener, so This is only triggered 
     *  when we send out a message
     */
    synchronized public void message(XNetReply l) {
        if (log.isDebugEnabled()) {
            log.debug("recieved message: " + l);
        }
        if (InternalState == OFFSENT) {
            // If an OFF was sent, we want to check for Communications
            // errors before we try to do anything else.
            if (l.isCommErrorMessage()) {
                /* this is a communications error */
                log.error("Communications error occured - message recieved was: "
                        + l);
                sendOffMessage();
                return;
            } else if (l.isCSBusyMessage()) {
                /* this is a communications error */
                log.error("Command station busy - message recieved was: " + l);
                sendOffMessage();
                return;
            } else if (l.isOkMessage()) {
                /* the command was successfully recieved */
                synchronized (this) {
                    //mOldState=mState;
                    InternalState = IDLE;
                }
                return;
            } else if (InternalState == COMMANDSENT) {
                // If command was sent,, we want to check for Communications
                // errors before we try to do anything else.
                if (l.isCommErrorMessage()) {
                    /* this is a communications error */
                    log.error("Communications error occured - message recieved was: "
                            + l);
                    setState(mState);
                    return;
                } else if (l.isCSBusyMessage()) {
                    /* this is a communications error */
                    log.error("Command station busy - message recieved was: " + l);
                    setState(mState);
                    return;
                } else if (l.isOkMessage()) {
                    /* the command was successfully recieved */
                    sendOffMessage();
                }
                return;
            }
        }
    }

    // listen for the messages to the LI100/LI101
    public void message(XNetMessage l) {
    }

    // Handle a timeout notification
    public void notifyTimeout(XNetMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    /* Send an "Off" message to the decoder for this output  */
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
        synchronized (this) {
            //mOldState=mState; 
            InternalState = OFFSENT;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(XNetLight.class.getName());
}

/* @(#)XNetLight.java */

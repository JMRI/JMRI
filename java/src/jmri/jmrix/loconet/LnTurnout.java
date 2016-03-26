// LnTurnout.java
package jmri.jmrix.loconet;

import jmri.NmraPacket;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractTurnout for LocoNet layouts
 * <P>
 * This implementation implements the "SENT" feedback, where LocoNet messages
 * originating on the layout can change both KnownState and CommandedState. We
 * change both because we consider a LocoNet message to reflect how the turnout
 * should be, even if its a readback status message. E.g. if you use a DS54
 * local input to change the state, resulting in a status message, we still
 * consider that to be a commanded state change.
 * <P>
 * Adds several additional feedback modes:
 * <UL>
 * <LI>MONITORING - listen to the LocoNet, so that commands from other LocoNet
 * sources (e.g. throttles) are properly reflected in the turnout state. This is
 * the default for LnTurnout objects as created.
 * <LI>INDIRECT - listen to the LocoNet for messages back from a DS54 that has a
 * microswitch attached to its Switch input.
 * <LI>EXACT - listen to the LocoNet for messages back from a DS54 that has two
 * microswitches, one connected to the Switch input and one to the Aux input.
 * </UL>
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 */
public class LnTurnout extends AbstractTurnout implements LocoNetListener {

    /**
     *
     */
    private static final long serialVersionUID = -8838048326340434647L;

    public LnTurnout(String prefix, int number, LocoNetInterface controller) throws IllegalArgumentException {
        // a human-readable turnout number must be specified!
        super(prefix + "T" + number);  // can't use prefix here, as still in construction
        log.debug("new turnout " + number);
        if (number < NmraPacket.accIdLowLimit || number > NmraPacket.accIdAltHighLimit) {
            throw new IllegalArgumentException("Turnout value: " + number 
                    + " not in the range " + NmraPacket.accIdLowLimit + " to " 
                    + NmraPacket.accIdAltHighLimit);
        }

        this.controller = controller;

        _number = number;
        // At construction, register for messages
        if (this.controller != null) {
            this.controller.addLocoNetListener(~0, this);
        } else {
            log.warn("No LocoNet connection, turnout won't update");
        }
        // update feedback modes
        _validFeedbackTypes |= MONITORING | EXACT | INDIRECT;
        _activeFeedbackType = MONITORING;

        // if needed, create the list of feedback mode
        // names with additional LocoNet-specific modes
        if (modeNames == null) {
            initFeedbackModes();
        }
        _validFeedbackNames = modeNames;
        _validFeedbackModes = modeValues;
    }

    LocoNetInterface controller;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "Only used during creation of 1st turnout")
    private void initFeedbackModes() {
        if (_validFeedbackNames.length != _validFeedbackModes.length) {
            log.error("int and string feedback arrays different length");
        }
        String[] tempModeNames = new String[_validFeedbackNames.length + 3];
        int[] tempModeValues = new int[_validFeedbackNames.length + 3];
        for (int i = 0; i < _validFeedbackNames.length; i++) {
            tempModeNames[i] = _validFeedbackNames[i];
            tempModeValues[i] = _validFeedbackModes[i];
        }
        tempModeNames[_validFeedbackNames.length] = "MONITORING";
        tempModeValues[_validFeedbackNames.length] = MONITORING;
        tempModeNames[_validFeedbackNames.length + 1] = "INDIRECT";
        tempModeValues[_validFeedbackNames.length + 1] = INDIRECT;
        tempModeNames[_validFeedbackNames.length + 2] = "EXACT";
        tempModeValues[_validFeedbackNames.length + 2] = EXACT;

        modeNames = tempModeNames;
        modeValues = tempModeValues;
    }

    static String[] modeNames = null;
    static int[] modeValues = null;

    public int getNumber() {
        return _number;
    }

    boolean _useOffSwReqAsConfirmation = false;
    public void setUseOffSwReqAsConfirmation(boolean state) {
        _useOffSwReqAsConfirmation = state;
    }

    // Handle a request to change state by sending a LocoNet command
    protected void forwardCommandChangeToLayout(final int newstate) {

        // send SWREQ for close/thrown ON
        sendOpcSwReqMessage(adjustStateForInversion(newstate), true);
        // schedule SWREQ for closed/thrown off, unless in basic mode
        if (!binaryOutput) {
            meterTimer.schedule(new java.util.TimerTask() {
                int state = newstate;

                public void run() {
                    try {
                        sendSetOffMessage(state);
                    } catch (Exception e) {
                        log.error("Exception occured while sending delayed off to turnout: " + e);
                    }
                }
            }, METERINTERVAL);
        }
    }

    /**
     * Send a single OPC_SW_REQ message for this turnout, with the CLOSED/THROWN
     * ON/OFF state.
     * <p>
     * Inversion is to already have been handled.
     */
    void sendOpcSwReqMessage(int state, boolean on) {
        LocoNetMessage l = new LocoNetMessage(4);
        l.setOpCode(LnConstants.OPC_SW_REQ);

        // compute address fields
        int hiadr = (_number - 1) / 128;
        int loadr = (_number - 1) - hiadr * 128;

        // set closed (note that this can't handle both!  Not sure how to
        // say that in LocoNet.
        if ((state & CLOSED) != 0) {
            hiadr |= 0x20;
            // thrown exception if also THROWN
            if ((state & THROWN) != 0) {
                log.error("LocoNet turnout logic can't handle both THROWN and CLOSED yet");
            }
        }

        // load On/Off
        if (on) {
            hiadr |= 0x10;
        } else {
            if (_useOffSwReqAsConfirmation) log.warn("Turnout "+_number+" is using OPC_SWREQ off as confirmation, but is sending OFF commands itself anyway");
            hiadr &= 0xEF;
        }

        // store and send
        l.setElement(1, loadr);
        l.setElement(2, hiadr);

        this.controller.sendLocoNetMessage(l);
        
        if (_useOffSwReqAsConfirmation) {
             // Start a timer to resend the command in a couple of seconds in case consistency is not obtained before then
             noConsistencyTimersRunning++;
             consistencyTimer.schedule(new java.util.TimerTask(){
                public void run() {
                    noConsistencyTimersRunning--;
                    if (!isConsistentState() && noConsistencyTimersRunning==0) {
                        log.debug("LnTurnout resending command for turnout "+_number);
                        forwardCommandChangeToLayout(getCommandedState());
    }
                }
             }, CONSISTENCYTIMER);
         }
    }

    boolean pending = false;

    /**
     * Set the turnout OFF, e.g. after a timeout
     */
    void sendSetOffMessage(int state) {
        sendOpcSwReqMessage(adjustStateForInversion(state), false);
    }

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //		public void firePropertyChange(String propertyName,
    //					      	Object oldValue,
    //						Object newValue)
    // _once_ if anything has changed state (or set the commanded state directly)
    public void message(LocoNetMessage l) {
        // parse message type
        switch (l.getOpCode()) {
            case LnConstants.OPC_SW_REQ: {               /* page 9 of Loconet PE */

                int sw1 = l.getElement(1);
                int sw2 = l.getElement(2);
                if (myAddress(sw1, sw2)) {
                    if (log.isDebugEnabled()) {
                        log.debug("SW_REQ received with valid address");
                    }
                    //sort out states
                    int state;
                    if ((sw2 & LnConstants.OPC_SW_REQ_DIR) != 0) {
                        state = CLOSED;
                    } else {
                        state = THROWN;
                    }
                    state = adjustStateForInversion(state);

                    newCommandedState(state);
                    boolean on = ((sw2 & LnConstants.OPC_SW_REQ_OUT) != 0);
                    if (getFeedbackMode()==MONITORING && !on || getFeedbackMode()==MONITORING && on && !_useOffSwReqAsConfirmation || getFeedbackMode()==DIRECT) {
                        newKnownState(state);
                    }
                }
                break;
            }
            case LnConstants.OPC_SW_REP: {               /* page 9 of Loconet PE */

                int sw1 = l.getElement(1);
                int sw2 = l.getElement(2);
                if (myAddress(sw1, sw2)) {
                    if (log.isDebugEnabled()) {
                        log.debug("SW_REP received with valid address");
                    }
                    // see if its a turnout state report
                    if ((sw2 & LnConstants.OPC_SW_REP_INPUTS) == 0) {
                        // LnConstants.OPC_SW_REP_INPUTS not set, these report outputs
                        // sort out states
                        int state;
                        state = sw2
                                & (LnConstants.OPC_SW_REP_CLOSED | LnConstants.OPC_SW_REP_THROWN);
                        state = adjustStateForInversion(state);

                        switch (state) {
                            case LnConstants.OPC_SW_REP_CLOSED:
                                newCommandedState(CLOSED);
                                if (getFeedbackMode() == MONITORING || getFeedbackMode() == DIRECT) {
                                    newKnownState(CLOSED);
                                }
                                break;
                            case LnConstants.OPC_SW_REP_THROWN:
                                newCommandedState(THROWN);
                                if (getFeedbackMode() == MONITORING || getFeedbackMode() == DIRECT) {
                                    newKnownState(THROWN);
                                }
                                break;
                            case LnConstants.OPC_SW_REP_CLOSED | LnConstants.OPC_SW_REP_THROWN:
                                newCommandedState(CLOSED + THROWN);
                                if (getFeedbackMode() == MONITORING || getFeedbackMode() == DIRECT) {
                                    newKnownState(CLOSED + THROWN);
                                }
                                break;
                            default:
                                newCommandedState(0);
                                if (getFeedbackMode() == MONITORING || getFeedbackMode() == DIRECT) {
                                    newKnownState(0);
                                }
                                break;
                        }
                    } else {
                        // LnConstants.OPC_SW_REP_INPUTS set, these are feedback messages from inputs
                        // sort out states
                        if ((sw2 & LnConstants.OPC_SW_REP_SW) != 0) {
                            // Switch input report
                            if ((sw2 & LnConstants.OPC_SW_REP_HI) != 0) {
                                // switch input closed (off)
                                if (getFeedbackMode() == EXACT) {
                                    // reached closed state
                                    newKnownState(adjustStateForInversion(CLOSED));
                                } else if (getFeedbackMode() == INDIRECT) {
                                    // reached closed state
                                    newKnownState(adjustStateForInversion(CLOSED));
                                }
                            } else {
                                // switch input thrown (input on)
                                if (getFeedbackMode() == EXACT) {
                                    // leaving CLOSED on way to THROWN, go INCONSISTENT if not already THROWN
                                    if (getKnownState() != THROWN) {
                                        newKnownState(INCONSISTENT);
                                    }
                                } else if (getFeedbackMode() == INDIRECT) {
                                    // reached thrown state
                                    newKnownState(adjustStateForInversion(THROWN));
                                }
                            }
                        } else {
                            // Aux input report
                            if ((sw2 & LnConstants.OPC_SW_REP_HI) != 0) {
                                // aux input closed (off)
                                if (getFeedbackMode() == EXACT) {
                                    // reached thrown state
                                    newKnownState(adjustStateForInversion(THROWN));
                                }
                            } else {
                                // aux input thrown (input on)
                                if (getFeedbackMode() == EXACT) {
                                    // leaving THROWN on the way to CLOSED, go INCONSISTENT if not already CLOSED
                                    if (getKnownState() != CLOSED) {
                                        newKnownState(INCONSISTENT);
                                    }
                                }
                            }
                        }

                    }
                }
                return;
            }
            default:
                return;
        }
        // reach here only in error
    }

    protected void turnoutPushbuttonLockout(boolean _pushButtonLockout) {
        if (log.isDebugEnabled()) {
            log.debug("Send command to " + (_pushButtonLockout ? "Lock" : "Unlock") + " Pushbutton LT" + _number);
        }
    }

    public void dispose() {
        this.controller.removeLocoNetListener(~0, this);
        super.dispose();
    }

    // data members
    int _number;   // loconet turnout number

    private boolean myAddress(int a1, int a2) {
        // the "+ 1" in the following converts to throttle-visible numbering
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1) == _number;
    }

    //ln turnouts do support inversion
    public boolean canInvert() {
        return true;
    }

    //method which takes a turnout state as a parameter and adjusts it  as necessary
    //to reflect the turnout invert property
    private int adjustStateForInversion(int rawState) {

        if (getInverted() && (rawState == CLOSED || rawState == THROWN)) {
            if (rawState == CLOSED) {
                return THROWN;
            } else {
                return CLOSED;
            }
        } else {
            return rawState;
        }

    }

    static final int METERINTERVAL = 100;  // msec wait before closed
    static java.util.Timer meterTimer = new java.util.Timer(true);

    static final int CONSISTENCYTIMER = 3000; // msec wait for command to take effect
    static java.util.Timer consistencyTimer = new java.util.Timer();
    int noConsistencyTimersRunning = 0;
    
    private final static Logger log = LoggerFactory.getLogger(LnTurnout.class.getName());

}

/* @(#)LnTurnout.java */

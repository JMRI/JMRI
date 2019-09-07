package jmri.jmrix.loconet;

import javax.annotation.*;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.NmraPacket;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractTurnout for LocoNet layouts
 * <p>
 * This implementation implements the "SENT" feedback, where LocoNet messages
 * originating on the layout can change both KnownState and CommandedState. We
 * change both because we consider a LocoNet message to reflect how the turnout
 * should be, even if it's a readback status message. E.g. if you use a DS54
 * local input to change the state, resulting in a status message, we still
 * consider that to be a commanded state change.
 * <p>
 * Adds several additional feedback modes:
 * <ul>
 *   <li>MONITORING - listen to the LocoNet, so that commands from other LocoNet
 *   sources (e.g. throttles) are properly reflected in the turnout state. This is
 *   the default for LnTurnout objects as created.
 *   <li>INDIRECT - listen to the LocoNet for messages back from a DS54 that has a
 *   microswitch attached to its Switch input.
 *   <li>EXACT - listen to the LocoNet for messages back from a DS54 that has two
 *   microswitches, one connected to the Switch input and one to the Aux input.
 * </ul>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class LnTurnout extends AbstractTurnout {

    public LnTurnout(String prefix, int number, LocoNetInterface controller) throws IllegalArgumentException {
        // a human-readable turnout number must be specified!
        super(prefix + "T" + number);  // can't use prefix here, as still in construction
        _prefix = prefix;
        log.debug("new turnout {}", number);
        if (number < NmraPacket.accIdLowLimit || number > NmraPacket.accIdAltHighLimit) {
            throw new IllegalArgumentException("Turnout value: " + number // NOI18N
                    + " not in the range " + NmraPacket.accIdLowLimit + " to " // NOI18N
                    + NmraPacket.accIdAltHighLimit);
        }

        this.controller = controller;

        _number = number;
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
    protected String _prefix = "L"; // default to "L"

    /**
     * True when setFeedbackMode has specified the mode;
     * false when the mode is just left over from initialization.
     * This is intended to indicate (when true) that a configuration
     * file has set the value; message-created turnouts have it false.
     */
    boolean feedbackDeliberatelySet = false; // package to allow access from LnTurnoutManager

    @Override
    public void setBinaryOutput(boolean state) {
        // TODO Auto-generated method stub
        setProperty(LnTurnoutManager.SENDONANDOFFKEY, !state);
        binaryOutput = state;
    }
    @Override
    public void setFeedbackMode(@Nonnull String mode) throws IllegalArgumentException {
        feedbackDeliberatelySet = true;
        super.setFeedbackMode(mode);
    }

    @Override
    public void setFeedbackMode(int mode) throws IllegalArgumentException {
        feedbackDeliberatelySet = true;
        super.setFeedbackMode(mode);
    }

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "Only used during creation of 1st turnout") // NOI18N
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
        tempModeNames[_validFeedbackNames.length] = "MONITORING"; // NOI18N
        tempModeValues[_validFeedbackNames.length] = MONITORING;
        tempModeNames[_validFeedbackNames.length + 1] = "INDIRECT"; // NOI18N
        tempModeValues[_validFeedbackNames.length + 1] = INDIRECT;
        tempModeNames[_validFeedbackNames.length + 2] = "EXACT"; // NOI18N
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

    public boolean isByPassBushbyBit() {
        Object returnVal = getProperty(LnTurnoutManager.BYPASSBUSHBYBITKEY);
        if (returnVal == null) {
            return  false;
        }
        return (boolean) returnVal;
    }

    public boolean isSendOnAndOff() {
        Object returnVal = getProperty(LnTurnoutManager.SENDONANDOFFKEY);
        if (returnVal == null) {
            return  true;
        }
        return (boolean) returnVal;
    }

    // Handle a request to change state by sending a LocoNet command
    @Override
    protected void forwardCommandChangeToLayout(final int newstate) {

        // send SWREQ for close/thrown ON
        sendOpcSwReqMessage(adjustStateForInversion(newstate), true);
        // schedule SWREQ for closed/thrown off, unless in basic mode
        if (isSendOnAndOff()) {
            meterTask = new java.util.TimerTask() {
                int state = newstate;

                @Override
                public void run() {
                    try {
                        sendSetOffMessage(state);
                    } catch (Exception e) {
                        log.error("Exception occurred while sending delayed off to turnout: " + e);
                    }
                }
            };
            jmri.util.TimerUtil.schedule(meterTask, METERINTERVAL);
        }
    }

    /**
     * Send a single OPC_SW_REQ message for this turnout, with the CLOSED/THROWN
     * ON/OFF state.
     * <p>
     * Inversion is to already have been handled.
     *
     * @param state the state to set
     * @param on    if true the C bit of the NMRA DCC packet is 1; if false the
     *              C bit is 0
     */
    void sendOpcSwReqMessage(int state, boolean on) {
        LocoNetMessage l = new LocoNetMessage(4);
        l.setOpCode(isByPassBushbyBit() ? LnConstants.OPC_SW_ACK : LnConstants.OPC_SW_REQ);
        int hiadr = ((_number - 1) / 128) & 0x7F;   // compute address fields
        l.setElement(1, ((_number - 1) - hiadr * 128) & 0x7F);

        // set closed bit (Note that LocoNet cannot handle both Thrown and Closed)
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
        } else if (_useOffSwReqAsConfirmation) {
            log.warn("Turnout {} is using OPC_SWREQ off as confirmation, but is sending OFF commands itself anyway", _number);
        }

        l.setElement(2, hiadr);

        this.controller.sendLocoNetMessage(l);  // send message

        if (_useOffSwReqAsConfirmation) {
            noConsistencyTimersRunning++;
            startConsistencyTimerTask();
        }
    }

    private void startConsistencyTimerTask() {
        // Start a timer to resend the command in a couple of seconds in case consistency is not obtained before then
        consistencyTask = new java.util.TimerTask() {
            @Override
            public void run() {
                noConsistencyTimersRunning--;
                if (!isConsistentState() && noConsistencyTimersRunning == 0) {
                    log.debug("LnTurnout resending command for turnout {}", _number);
                    forwardCommandChangeToLayout(getCommandedState());
                }
            }
        };
        jmri.util.TimerUtil.schedule(consistencyTask, CONSISTENCYTIMER);
    }

    boolean pending = false;

    /**
     * Set the turnout DCC C bit to OFF. This is typically used to set a C bit
     * that was set ON to OFF after a timeout.
     *
     * @param state the turnout state
     */
    void sendSetOffMessage(int state) {
        sendOpcSwReqMessage(adjustStateForInversion(state), false);
    }

    private void handleReceivedOpSwAckReq(LocoNetMessage l) {
        int sw2 = l.getElement(2);
        if (myAddress(l.getElement(1), sw2)) {

            log.debug("SW_REQ received with valid address");
            //sort out states
            int state;
            state = ((sw2 & LnConstants.OPC_SW_REQ_DIR) != 0) ? CLOSED : THROWN;
            state = adjustStateForInversion(state);

            newCommandedState(state);
            computeKnownStateOpSwAckReq(sw2, state);
        }
    }

    private void computeKnownStateOpSwAckReq(int sw2, int state) {
        boolean on = ((sw2 & LnConstants.OPC_SW_REQ_OUT) != 0);
        switch (getFeedbackMode()) {
            case MONITORING:
                if ((!on) || (!_useOffSwReqAsConfirmation)) {
                    newKnownState(state);
                }
                break;
            case DIRECT:
                newKnownState(state);
                break;
            default:
                break;                    
        }

    }
    private void setKnownStateFromOutputStateClosedReport() {
        newCommandedState(CLOSED);
        if (getFeedbackMode() == MONITORING || getFeedbackMode() == DIRECT) {
            newKnownState(CLOSED);
        }
    }
    
    private void setKnownStateFromOutputStateThrownReport() {
        newCommandedState(THROWN);
        if (getFeedbackMode() == MONITORING || getFeedbackMode() == DIRECT) {
            newKnownState(THROWN);
        }
    }
    
    private void setKnownStateFromOutputStateOddReport() {
        newCommandedState(CLOSED + THROWN);
        if (getFeedbackMode() == MONITORING || getFeedbackMode() == DIRECT) {
            newKnownState(CLOSED + THROWN);
        }
    }
    
    private void setKnownStateFromOutputStateReallyOddReport() {
        newCommandedState(0);
        if (getFeedbackMode() == MONITORING || getFeedbackMode() == DIRECT) {
            newKnownState(0);
        }
    }
    
    private void computeFromOutputStateReport(int sw2) {
        // LnConstants.OPC_SW_REP_INPUTS not set, these report outputs
        // sort out states
        int state;
        state = sw2
                & (LnConstants.OPC_SW_REP_CLOSED | LnConstants.OPC_SW_REP_THROWN);
        state = adjustStateForInversion(state);

        switch (state) {
            case LnConstants.OPC_SW_REP_CLOSED:
                setKnownStateFromOutputStateClosedReport();
                break;
            case LnConstants.OPC_SW_REP_THROWN:
                setKnownStateFromOutputStateThrownReport();
                break;
            case LnConstants.OPC_SW_REP_CLOSED | LnConstants.OPC_SW_REP_THROWN:
                setKnownStateFromOutputStateOddReport();
                break;
            default:
                setKnownStateFromOutputStateReallyOddReport();
                break;
        }
    }

    private void computeFeedbackFromSwitchReport(int sw2) {
        // Switch input report
        if ((sw2 & LnConstants.OPC_SW_REP_HI) != 0) {
            computeFeedbackFromSwitchOffReport();
        } else {
            computeFeedbackFromSwitchOnReport();
        }
    }
    
    private void computeFeedbackFromSwitchOffReport() {
        // switch input closed (off)
        if (getFeedbackMode() == EXACT) {
            // reached closed state
            newKnownState(adjustStateForInversion(CLOSED));
        } else if (getFeedbackMode() == INDIRECT) {
            // reached closed state
            newKnownState(adjustStateForInversion(CLOSED));
        } else if (!feedbackDeliberatelySet) {
            // don't have a defined feedback mode, but know we've reached closed state
            log.debug("setting CLOSED with !feedbackDeliberatelySet");
            newKnownState(adjustStateForInversion(CLOSED));
        }
    }

    private void computeFeedbackFromSwitchOnReport() {
        // switch input thrown (input on)
        if (getFeedbackMode() == EXACT) {
            // leaving CLOSED on way to THROWN, go INCONSISTENT if not already THROWN
            if (getKnownState() != THROWN) {
                newKnownState(INCONSISTENT);
            }
        } else if (getFeedbackMode() == INDIRECT) {
            // reached thrown state
            newKnownState(adjustStateForInversion(THROWN));
        } else if (!feedbackDeliberatelySet) {
            // don't have a defined feedback mode, but know we're not in closed state, most likely is actually thrown
            log.debug("setting THROWN with !feedbackDeliberatelySet");
            newKnownState(adjustStateForInversion(THROWN));
        }
    }

    private void computeFromSwFeedbackState(int sw2) {
        // LnConstants.OPC_SW_REP_INPUTS set, these are feedback messages from inputs
        // sort out states
        if ((sw2 & LnConstants.OPC_SW_REP_SW) != 0) {
            computeFeedbackFromSwitchReport(sw2);

        } else {
            computeFeedbackFromAuxInputReport(sw2);
        }
    }

    private void computeFeedbackFromAuxInputReport(int sw2) {
        // This is only valid in EXACT mode, so if we encounter it
        //  without a feedback mode set, we switch to EXACT
        if (!feedbackDeliberatelySet) {
            setFeedbackMode(EXACT);
            feedbackDeliberatelySet = false; // was set when setting feedback
        }

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

    private void handleReceivedOpSwRep(LocoNetMessage l) {
        int sw1 = l.getElement(1);
        int sw2 = l.getElement(2);
        if (myAddress(sw1, sw2)) {

            log.debug("SW_REP received with valid address");
            // see if its a turnout state report
            if ((sw2 & LnConstants.OPC_SW_REP_INPUTS) == 0) {
                computeFromOutputStateReport(sw2);
            } else {
                computeFromSwFeedbackState(sw2);
            }
        }
    }

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //        public void firePropertyChange(String propertyName,
    //                              Object oldValue,
    //                        Object newValue)
    // _once_ if anything has changed state (or set the commanded state directly)
    public void messageFromManager(LocoNetMessage l) {
        // parse message type
        switch (l.getOpCode()) {
            case LnConstants.OPC_SW_ACK:
            case LnConstants.OPC_SW_REQ: {
                handleReceivedOpSwAckReq(l);
                return;
                }
            case LnConstants.OPC_SW_REP: {
                handleReceivedOpSwRep(l);
                return;
            }
            default:
                return;
        }
    }

    @Override
    protected void turnoutPushbuttonLockout(boolean _pushButtonLockout) {
        if (log.isDebugEnabled()) {
            log.debug("Send command to {} Pushbutton {}T{}", (_pushButtonLockout ? "Lock" : "Unlock"), _prefix, _number);
        }
    }

    @Override
    public void dispose() {
        if(meterTask!=null) {
           meterTask.cancel();
        }
        if(consistencyTask != null ) {
           consistencyTask.cancel();
        }
        super.dispose();
    }

    // data members
    int _number;   // LocoNet Turnout number

    private boolean myAddress(int a1, int a2) {
        // the "+ 1" in the following converts to throttle-visible numbering
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1) == _number;
    }

    //ln turnouts do support inversion
    @Override
    public boolean canInvert() {
        return true;
    }

    /**
     * Take a turnout state as a parameter and adjusts it as necessary
     * to reflect the turnout "Invert" property.
     *
     * @param rawState "original" turnout state before optional inverting
     */
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
    private java.util.TimerTask meterTask = null;

    static final int CONSISTENCYTIMER = 3000; // msec wait for command to take effect
    int noConsistencyTimersRunning = 0;
    private java.util.TimerTask consistencyTask = null;

    private final static Logger log = LoggerFactory.getLogger(LnTurnout.class);

}

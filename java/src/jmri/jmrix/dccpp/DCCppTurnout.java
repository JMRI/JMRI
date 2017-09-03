package jmri.jmrix.dccpp;

import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends jmri.AbstractTurnout for DCCpp layouts
 * <P>
 * Turnouts on DCC++ are controlled (as of V1.5 Firmware) are controlled
 * with unidirectional Stationary Decoder commands, or with bidirectional
 * (predefined) Turnout commands, or with bidirectional (predefined) Output
 * commands.
 * 
 * DCC++ Has three ways to activate a turnout (output)
 * <ul>
 * <li> Accessory Command "a" : sends a DCC packet to a stationary decoder
 *      out there on the bus somewhere. NO RETURN VALUE to JMRI.
 * </li>
 * <li> Turnout Command "T" : Looks up a DCC address from an internal table
 *      in the Base Station and sends that Stationary Decoder a packet.  Returns
 *      a (basically faked) "H" response to JMRI indicating the (supposed)
 *      current state of the turnout.  Or "X" if the indexed turnout is not in
 *      the list.
 * </li>
 * <li> Output Command "z" : Looks up a Base Station Arduino Pin number from
 *      an internal lookup table, and sets/toggles the state of that pin.  
 *      Returns a "Y" response indicating the actual state of the pin.  Or "X"
 *      if the indexed pin is not in the list.
 * </li>
 * </ul>
 * 
 * The DCCppTurnout supports three types of feedback:
 * <ul>
 * <li> DIRECT:  No actual feedback, uses Stationary Decoder command and
 *      fakes the response.
 * </li>
 * <li> MONITORING: Uses the Turnout command, lets the Base Station
 *      fake the response :) 
 * </li>
 * <li> EXACT: Uses the Output command to directly address an Arduino pin.
 * </li>
 * </ul>
 *
 * It also supports "NO FEEDBACK" by treating it like "DIRECT".
 * 
 * Turnout operation on DCC++ based systems goes through the following
 * sequence:
 * <ul>
 * <li> set the commanded state, and, Send request to command station to start
 * sending DCC operations packet to track</li>
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author      Paul Bender Copyright (C) 2003-2010
 * @author      Mark Underwood Copyright (C) 2015
 *
 * Based on lenz.XNetTurnout by Bob Jacobsen and Paul Bender
 */
public class DCCppTurnout extends AbstractTurnout implements DCCppListener {

    /* State information */
    protected static final int COMMANDSENT = 2;
    protected static final int STATUSREQUESTSENT = 4;
    protected static final int IDLE = 0;
    protected int internalState = IDLE;

    /* Static arrays to hold DCC++ specific feedback mode information */
    static String[] modeNames = null;
    static int[] modeValues = null;

    //@SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC")
    //protected int _mThrown = jmri.Turnout.THROWN;
    //@SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC")
    //protected int _mClosed = jmri.Turnout.CLOSED;

    protected String _prefix = "DCCPP"; // TODO: Shouldn't this be fetched from the system memo?
    protected DCCppTrafficController tc = null;

    public DCCppTurnout(String prefix, int pNumber, DCCppTrafficController controller) {  // a human-readable turnout number must be specified!
        super(prefix + "T" + pNumber);
        tc = controller;
        _prefix = prefix;
        mNumber = pNumber; // this is the address.

        /* Add additional feedback types information */
        // Note DIRECT, ONESENSOR and TWOSENSOR are already OR'ed in.
        _validFeedbackTypes |= MONITORING;   // uses the Turnout command <T...>
        _validFeedbackTypes |= EXACT; // uses the Output command <z...>
        
        // Default feedback mode is DIRECT
        _activeFeedbackType = DIRECT;
        
        setModeInformation(_validFeedbackNames, _validFeedbackModes);
        
        // set the mode names and values based on the static values.
        _validFeedbackNames = getModeNames();
        _validFeedbackModes = getModeValues();
        
        // Register to get property change information from the superclass
        _stateListener = new DCCppTurnoutStateListener(this);
        this.addPropertyChangeListener(_stateListener);
        // Finally, request the current state from the layout.
        tc.getTurnoutReplyCache().requestCachedStateFromLayout(this);
    }

    //Set the mode information for DCC++ Turnouts.
    synchronized static private void setModeInformation(String[] feedbackNames, int[] feedbackModes) {
        // if it hasn't been done already, create static arrays to hold 
        // the DCC++ specific feedback information.
        if (modeNames == null) {
            if (feedbackNames.length != feedbackModes.length) {
                log.error("int and string feedback arrays different length");
            }
            // NOTE: What we are doing here is tacking extra modes to the list
            // *beyond* the defaults of DIRECT, ONESENSOR and TWOSENSOR
            modeNames = new String[feedbackNames.length + 2];
            modeValues = new int[feedbackNames.length + 2];
            for (int i = 0; i < feedbackNames.length; i++) {
                modeNames[i] = feedbackNames[i];
                modeValues[i] = feedbackModes[i];
            }
            modeNames[feedbackNames.length] = "BSTURNOUT";
            modeValues[feedbackNames.length] = MONITORING;
            modeNames[feedbackNames.length+1] = "BSOUTPUT";
            modeValues[feedbackNames.length+1] = EXACT;
        }
    }

    static int[] getModeValues() {
        return modeValues;
    }

    static String[] getModeNames() {
        return modeNames;
    }

    public int getNumber() {
        return mNumber;
    }

    // Set the Commanded State.   This method overrides setCommandedState in 
    // the Abstract Turnout class.
    /*
    //@Override
    //public void setCommandedState(int s) {
    //    if (log.isDebugEnabled()) {
    //        log.debug("set commanded state for turnout " + getSystemName() + " to " + s);
    //    }
    //    synchronized (this) {
    //        newCommandedState(s);
    //    }
    //    myOperator = getTurnoutOperator();        // MUST set myOperator before starting the thread
    //    if (myOperator == null) {
    //        forwardCommandChangeToLayout(s);
    //        synchronized (this) {
    //            newKnownState(INCONSISTENT);
    //        }
    //    } else {
    //        myOperator.start();
    //    }
    //
    //}
    */

    @Override
    public void setCommandedState(int s) {
        if (log.isDebugEnabled()) {
            log.debug("set commanded state for turnout " + getSystemName() + " to " + s);
        }
        synchronized (this) {
            newCommandedState(s);
        }
        forwardCommandChangeToLayout(s);
        // Only set the known state to inconsistent if we actually expect a response
        // from the Base Station
        if (_activeFeedbackType == EXACT || _activeFeedbackType == MONITORING) {
            synchronized (this) {
                newKnownState(INCONSISTENT);
            }
        }
    }

    // Handle a request to change state by sending a DCC++ command
    @Override
    synchronized protected void forwardCommandChangeToLayout(int s) {
        DCCppMessage msg;
        if (s != CLOSED && s != THROWN) {
            log.warn("Turnout " + mNumber + ": state " + s + " not forwarded to layout.");
            return;
        }
        // newstate = TRUE if s == THROWN ...
        // ... unless we are inverted, then newstate = TRUE if s == CLOSED
        boolean newstate = (s == THROWN);
        if (getInverted()) {
            newstate = !newstate;
        }
        switch (_activeFeedbackType) {
        case EXACT: // Use <z ... > command
            // mNumber is the index ID into the Base Station's internal table of outputs.
            // Convert the integer Turnout value to boolean for DCC++ internal code.
            // Assume if it's not THROWN (true), it must be CLOSED (false).
            msg = DCCppMessage.makeOutputCmdMsg(mNumber, newstate);
            internalState = COMMANDSENT;
            break;
        case MONITORING: // Use <T ... > command
            // mNumber is the index ID into the Base Station's internal table of Turnouts.
            // Convert the integer Turnout value to boolean for DCC++ internal code.
            // Assume if it's not THROWN (true), it must be CLOSED (false).
            msg = DCCppMessage.makeTurnoutCommandMsg(mNumber, newstate);
            internalState = COMMANDSENT;
            break;
        default: // DIRECT -- use <a ... > command
            // mNumber is the DCC address of the device.
            // Convert the integer Turnout value to boolean for DCC++ internal code.
            // Assume if it's not THROWN (true), it must be CLOSED (false).
            msg = DCCppMessage.makeAccessoryDecoderMsg(mNumber, newstate);
            internalState = IDLE; // change this!
            break;
            
        }
        log.debug("Sending Message: {}", msg.toString());
        tc.sendDCCppMessage(msg, this);
    }
    
    @Override
    protected void turnoutPushbuttonLockout(boolean _pushButtonLockout) {
        if (log.isDebugEnabled()) {
            log.debug("Send command to " + (_pushButtonLockout ? "Lock" : "Unlock") + " Pushbutton " + _prefix + "T" + mNumber);
        }
    }
    
    /**
     * request an update on status by sending a DCC++ message
     */
    public void requestUpdateFromLayout() {
        // Yeah, umm... DCC++ doesn't do this... yet.
        // (02/2017) Yes it does... using the <s> command or possibly
        // some others.  TODO: Plumb this in... IFF it is needed.
        
        // To do this, we send an XPressNet Accessory Decoder Information
        // Request.
        // The generated message works for Feedback modules and turnouts
        // with feedback, but the address passed is translated as though it
        // is a turnout address.  As a result, we substitute our base
        // address in for the address. after the message is returned.

        return;
        /*
        // DCCppMessage msg = DCCppMessage.getFeedbackRequestMsg(mNumber,
        //         ((mNumber - 1) % 4) < 2);
        // synchronized (this) {
        //     internalState = STATUSREQUESTSENT;
        // }
        // tc.sendDCCppMessage(msg, null); //status is returned via the manager.
        */

    }

    /*
    //@Override
    //synchronized public void setInverted(boolean inverted) {
    //    if (log.isDebugEnabled()) {
    //        log.debug("Inverting Turnout State for turnout xt" + mNumber);
    //    }
    //    _inverted = inverted;
    //    if (inverted) {
    //        _mThrown = jmri.Turnout.CLOSED;
    //        _mClosed = jmri.Turnout.THROWN;
    //    } else {
    //        _mThrown = jmri.Turnout.THROWN;
    //        _mClosed = jmri.Turnout.CLOSED;
    //    }
    //    super.setInverted(inverted);
    //}
    */

    @Override
    public boolean canInvert() {
        return true;
    }

    /**
     * initmessage is a package proteceted class which allows the Manger to send
     * a feedback message at initilization without changing the state of the
     * turnout with respect to whether or not a feedback request was sent. This
     * is used only when the turnout is created by on layout feedback.
     *
     *
     */
    synchronized void initmessage(DCCppReply l) {
        int oldState = internalState;
        message(l);
        internalState = oldState;
    }


    /*
     *  Handle an incoming message from the DCC++
     */
    @Override
    synchronized public void message(DCCppReply l) {
        if (log.isDebugEnabled()) {
            log.debug("recieved message: " + l);
        }

        switch (getFeedbackMode()) {
        case EXACT:
            handleExactModeFeedback(l);
            // IS THIS FALL THROUGH INTENTIONAL? SEEMS ODD
            break;
        case MONITORING:
            handleMonitoringModeFeedback(l);
            break;
        case DIRECT:
        default:
            // Default is direct mode - we should never get here, actually.
            handleDirectModeFeedback(l);
        }
    }

    // listen for the messages to the LI100/LI101
    @Override
    public void message(DCCppMessage l) {
    }

    // Handle a timeout notification
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    /*
     * With DIRECT mode we don't actually expect a response from the
     * Base Station, but we'll leave this code in, in case the Base Station
     * implements an ACK or OK response later.
     */
    synchronized private void handleDirectModeFeedback(DCCppReply l) {
        /* If commanded state does not equal known state, we are 
         going to check to see if one of the following conditions 
         applies:
         1) The recieved message is a feedback message for a turnout
         and one of the two addresses to which it applies is our 
         address
         2) We recieve an "OK" message, indicating the command was 
         successfully sent
           
         If either of these two cases occur, we trigger an off message
         */

        if (log.isDebugEnabled()) {
            log.debug("Handle Message for turnout "
                      + mNumber + " in DIRECT feedback mode ");
        }
        if (getCommandedState() != getKnownState() || internalState == COMMANDSENT) {
            if (l.isTurnoutReply() && l.getTOIDInt() == mNumber) {
                // This message includes feedback for this turnout  
                if (log.isDebugEnabled()) {
                    log.debug("Turnout " + mNumber + " DIRECT feedback mode - directed reply received.");
                }
            }
        }
        return;
    }

    /*
     *  With Monitoring Mode feedback, if we see a feedback message, we 
     *  interpret that message and use it to display our feedback. 
     *  <P> 
     *  After we send a request to operate a turnout, We ask the command 
     *  station to stop sending information to the stationary decoder
     *  when the either a feedback message or an "OK" message is recieved.
     *
     *  @param l an {@link DCCppReply} message
     *
     */
    synchronized private void handleMonitoringModeFeedback(DCCppReply l) {
        /* In Monitoring Mode, We have two cases to check if CommandedState 
           does not equal KnownState, otherwise, we only want to check to 
           see if the messages we recieve indicate this turnout chagned 
           state
        */
        if (log.isDebugEnabled()) {
            log.debug("Handle Message for turnout "
                      + mNumber + " in MONITORING feedback mode ");
        }
        //if(getCommandedState()==getKnownState() && internalState==IDLE) {
        if (internalState == IDLE || internalState == STATUSREQUESTSENT) {
            log.debug("In Idle or StatusRequestSent State");
            if (l.isTurnoutReply() && (l.getTOIDInt() == mNumber)) {
                // This is a feedback message, we need to check and see if it
                // indicates this turnout is to change state or if it is for 
                // another turnout.
                log.debug("Turnout " + mNumber + " MONITORING feedback mode - state change from feedback.");
            }
        } else if (getCommandedState() != getKnownState()
                   || internalState == COMMANDSENT) {
            log.debug ("In inconsistent or COMMANDSENT state toID = " + l.getTOIDInt());
            if (l.isTurnoutReply() && (l.getTOIDInt() == mNumber)) {
                // In Monitoring mode, treat both turnouts with feedback 
                // and turnouts without feedback as turnouts without 
                // feedback.  i.e. just interpret the feedback 
                // message, don't check to see if the motion is complete
                if (parseMonitoringFeedbackMessage(l, 0) != -1) {
                    // We need to tell the turnout to shut off the output.
                    // No, we don't... we're just pretending here...
                    if (log.isDebugEnabled()) {
                        log.debug("Turnout " + mNumber + " MONITORING feedback mode - state change from feedback, CommandedState != KnownState.");
                    }
                }
            }
        }
        return;
    }
    
    synchronized private void handleExactModeFeedback(DCCppReply l) {
        /* In Exact Mode, We have two cases to check if CommandedState 
           does not equal KnownState, otherwise, we only want to check to 
           see if the messages we recieve indicate this turnout chagned 
           state
        */
        if (log.isDebugEnabled()) {
            log.debug("Handle Message for turnout "
                      + mNumber + " in EXACT feedback mode ");
        }
        //if(getCommandedState()==getKnownState() && internalState==IDLE) {
        if (internalState == IDLE || internalState == STATUSREQUESTSENT) {
            if (l.isOutputCmdReply() && (l.getOutputNumInt() == mNumber)) {
                // This is a feedback message, we need to check and see if it
                // indicates this turnout is to change state or if it is for 
                // another turnout.
                log.debug("Turnout " + mNumber + " EXACT feedback mode - state change from feedback.");
            }
        } else if (getCommandedState() != getKnownState()
                   || internalState == COMMANDSENT) {
            if (l.isOutputCmdReply() && (l.getOutputNumInt() == mNumber)) {
                // In Exact mode, treat both turnouts with feedback 
                // and turnouts without feedback as turnouts without 
                // feedback.  i.e. just interpret the feedback 
                // message, don't check to see if the motion is complete
                if (parseExactFeedbackMessage(l, 0) != -1) {
                    // We need to tell the turnout to shut off the output.
                    // No we don't we're just pretending...
                    if (log.isDebugEnabled()) {
                        log.debug("Turnout " + mNumber + " EXACT feedback mode - state change from feedback, CommandedState != KnownState.");
                    }
                }
            }
        }
        return;
    }

    /*
     * parse the feedback message, and set the status of the turnout 
     * accordingly
     *
     * @param l - feedback broadcast message
     * @param startByte - first Byte of message to check (kind of ignored)
     * 
     * @return 0 if address matches our turnout -1 otherwise
     */
    synchronized private int parseMonitoringFeedbackMessage(DCCppReply l, int startByte) {
        // check validity & addressing
        if (l.getTOIDInt() == mNumber) {
            // is for this object, parse the message
            if (log.isDebugEnabled()) {
                log.debug("Message for turnout " + mNumber);
            }
            if (l.getTOIsThrown()) {
                log.debug("Turnout is Thrown. Inverted = " + (getInverted() ? "True" : "False"));
                synchronized (this) {
                    newCommandedState(getInverted() ? CLOSED : THROWN);
                    newKnownState(getCommandedState());
                }
                return (0);
            } else if (l.getTOIsClosed()) {
                log.debug("Turnout is Closed. Inverted = " + (getInverted() ? "True" : "False"));
                synchronized (this) {
                    newCommandedState(getInverted() ? THROWN : CLOSED);
                    newKnownState(getCommandedState());
                }
                return (0);
            } else {
                log.debug("Turnout is inconsistent. Commanded = " + getCommandedState() + " Known = " + getKnownState());
                // the state is unknown or inconsistent.  If the command state 
                // does not equal the known state, and the command repeat the 
                // last command
                //
                // This should never happen in the current version of DCC++
                if (getCommandedState() != getKnownState()) {
                    forwardCommandChangeToLayout(getCommandedState());
                }
                return -1;
            }
        }
        return (-1);
    }
    
    /** parseExactFeedbackMessage()
     * parse the feedback message, and set the status of the turnout 
     * accordingly
     *
     * Note for Outputs (EXACT mode), LOW is THROWN, HIGH is CLOSED
     * As defined in DCC++ Base Station SerialCommand.cpp
     *
     * @param l - feedback broadcast message
     * @param startByte - first Byte of message to check (kind of ignored)
     * 
     * @return 0 if address matches our turnout -1 otherwise
     */
    synchronized private int parseExactFeedbackMessage(DCCppReply l, int startByte) {
        // check validity & addressing
        if (l.getOutputNumInt() == mNumber) {
            // is for this object, parse the message
            if (log.isDebugEnabled()) {
                log.debug("Message for turnout " + mNumber);
            }
            if (l.getOutputIsLow()) {
                log.debug("Turnout is Thrown. Inverted = " + (getInverted() ? "True" : "False"));
                synchronized (this) {
                    newCommandedState(getInverted() ? CLOSED : THROWN);
                    newKnownState(getCommandedState());
                }
                return (0);
            } else if (l.getOutputIsHigh()) {
                log.debug("Turnout is Closed. Inverted = " + (getInverted() ? "True" : "False"));
                synchronized (this) {
                    newCommandedState(getInverted() ? THROWN : CLOSED);
                    newKnownState(getCommandedState());
                }
                return (0);
            } else {
                log.debug("Turnout is inconsistent. Commanded = " + getCommandedState() + " Known = " + getKnownState());
                // the state is unknown or inconsistent.  If the command state 
                // does not equal the known state, and the command repeat the 
                // last command
                //
                // This should never happen in the current version of DCC++
                if (getCommandedState() != getKnownState()) {
                    forwardCommandChangeToLayout(getCommandedState());
                }
                return -1;
            }
        }
        return (-1);
    }
    
    @Override
    public void dispose() {
        this.removePropertyChangeListener(_stateListener);
        super.dispose();
    }
    
    // Internal class to use for listening to state changes
    private static class DCCppTurnoutStateListener implements java.beans.PropertyChangeListener {
        
        DCCppTurnout _turnout = null;
        
        DCCppTurnoutStateListener(DCCppTurnout turnout) {
            _turnout = turnout;
        }
        
        /*
         * If we're  not using DIRECT feedback mode, we need to listen for 
         * state changes to know when to send an OFF message after we set the 
         * known state
         * If we're using DIRECT mode, all of this is handled from the 
         * XPressNet Messages
         */
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent event) {
            if (log.isDebugEnabled()) {
                log.debug("propertyChange called");
            }
            // If we're using DIRECT feedback mode, we don't care what we see here
            if (_turnout.getFeedbackMode() != DIRECT) {
                if (log.isDebugEnabled()) {
                    log.debug("propertyChange Not Direct Mode property: " + event.getPropertyName() + " old value " + event.getOldValue() + " new value " + event.getNewValue());
                }
                if (event.getPropertyName().equals("KnownState")) {
                    // Check to see if this is a change in the status 
                    // triggered by a device on the layout, or a change in 
                    // status we triggered.
                    int oldKnownState = ((Integer) event.getOldValue()).intValue();
                    int curKnownState = ((Integer) event.getNewValue()).intValue();
                    if (log.isDebugEnabled()) {
                        log.debug("propertyChange KnownState - old value " + oldKnownState + " new value " + curKnownState);
                    }
                    if (curKnownState != INCONSISTENT
                        && _turnout.getCommandedState() == oldKnownState) {
                        // This was triggered by feedback on the layout, change 
                        // the commanded state to reflect the new Known State
                        if (log.isDebugEnabled()) {
                            log.debug("propertyChange CommandedState: " + _turnout.getCommandedState());
                        }
                        _turnout.newCommandedState(curKnownState);
                    } else {
                        // Since we always set the KnownState to 
                        // INCONSISTENT when we send a command, If the old 
                        // known state is INCONSISTENT, we just want to send 
                        // an off message
                        if (oldKnownState == INCONSISTENT) {
                            if (log.isDebugEnabled()) {
                                log.debug("propertyChange CommandedState: " + _turnout.getCommandedState());
                            }
                        }
                    }
                }
            }
        }
        
    }
    
    // data members
    protected int mNumber;   // XpressNet turnout number
    DCCppTurnoutStateListener _stateListener;  // Internal class object
    
    private final static Logger log = LoggerFactory.getLogger(DCCppTurnout.class);
    
}

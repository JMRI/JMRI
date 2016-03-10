/**
 * DCCppTurnout.java
 *
 * Description:	extend jmri.AbstractTurnout for DCCpp layouts
 * <P>
 * Turnouts on DCC++ are controlled (as of V1.5 Firmware) are controlled
 * with unidirectional Stationary Decoder commands, or with bidirectional
 * (predefined) Turnout commands.  Since at the time of this code writing
 * (Nov. 2015) there is no programmatic way of doing MADC on the predefined
 * turnouts, JMRI support for DCC++ turnouts will (for now) be only through
 * Stationary Decoder commands.
 *
 * This inherently means, no feedback from the layout.
 *
 * However, to the extent practical, the JMRI code framework for turnout
 * feedback will be preserved.  It is anticipated that soon, either of
 * two things will be done to the DCC++ firmware:
 * (a) state feedback will be added to Stationary Decoder commands
 * or
 * (b) MADC commands will be added to support Predefined Turnouts.
 *
 * The DCCppTurnout supports two types of feedback:
 * <ul>
 * <li> DIRECT:  No actual feedback, uses Stationary Decoder command and
 *      fakes the response.</li>
 * <li> MONITORING: Uses the Turnout command, lets the Base Station
 *      fake the response :) </li>
 *
 *
 * Turnout operation on DCC++ based systems goes through the following
 * sequence:
 * <UL>
 * <LI> set the commanded state, and, Send request to command station to start
 * sending DCC operations packet to track</LI>
 * </UL>
 * <P>
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @author      Paul Bender Copyright (C) 2003-2010
 * @author      Mark Underwood Copyright (C) 2015
 * @version	$Revision$
 *
 * Based on lenz.XNetTurnout by Bob Jacobsen and Paul Bender
 */
package jmri.jmrix.dccpp;

import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DCCppTurnout extends AbstractTurnout implements DCCppListener {

    /* State information */
    protected static final int COMMANDSENT = 2;
    protected static final int STATUSREQUESTSENT = 4;
    protected static final int IDLE = 0;
    protected int internalState = IDLE;

    /* Static arrays to hold Lenz specific feedback mode information */
    static String[] modeNames = null;
    static int[] modeValues = null;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC")
    protected int _mThrown = jmri.Turnout.THROWN;
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC")
    protected int _mClosed = jmri.Turnout.CLOSED;

    protected String _prefix = "DCCPP"; // TODO: Shouldn't this be fetched from the system memo?
    protected DCCppTrafficController tc = null;

    public DCCppTurnout(String prefix, int pNumber, DCCppTrafficController controller) {  // a human-readable turnout number must be specified!
        super(prefix + "T" + pNumber);
        tc = controller;
        _prefix = prefix;
        mNumber = pNumber; // this is the address.

        /* Add additional feedback types information */
	// Note DIRECT, ONESENSOR and TWOSENSOR are already OR'ed in.
	_validFeedbackTypes |= MONITORING;   // uses the Turnout command <t...>
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
            modeNames[feedbackNames.length] = "MONITORING";
            modeValues[feedbackNames.length] = MONITORING;
            modeNames[feedbackNames.length+1] = "EXACT";
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
    public void setCommandedState(int s) {
        if (log.isDebugEnabled()) {
            log.debug("set commanded state for turnout " + getSystemName() + " to " + s);
        }
        synchronized (this) {
            newCommandedState(s);
        }
        myOperator = getTurnoutOperator();        // MUST set myOperator before starting the thread
        if (myOperator == null) {
            forwardCommandChangeToLayout(s);
            synchronized (this) {
                newKnownState(INCONSISTENT);
            }
        } else {
            myOperator.start();
        }

    }

    // Handle a request to change state by sending a DCC++ command
    synchronized protected void forwardCommandChangeToLayout(int s) {
	DCCppMessage msg;
        if (s != _mClosed && s != _mThrown) {
            log.warn("Turnout " + mNumber + ": state " + s + " not forwarded to layout.");
            return;
        }
        if (_activeFeedbackType == EXACT) {
            msg = DCCppMessage.makeOutputCmdMsg(mNumber, (s == THROWN));
            internalState = COMMANDSENT;
        } else if (_activeFeedbackType == MONITORING) {

	    // Convert the integer Turnout value to boolean for DCC++ internal code.
	    // Assume if it's not THROWN (true), it must be CLOSED (false).
	    msg = DCCppMessage.makeTurnoutCommandMsg(mNumber, (s == THROWN));
	    internalState = COMMANDSENT;
	} else { // Assume Direct Mode
	    int addr = (mNumber -1) / 4 + 1;
	    int sub = (mNumber - 1) % 4;
	    msg = DCCppMessage.makeAccessoryDecoderMsg(addr, sub, (s == THROWN));
	    internalState = IDLE; // change this!
	}
	tc.sendDCCppMessage(msg, this);
    }

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

        // To do this, we send an XpressNet Accessory Decoder Information
        // Request.
        // The generated message works for Feedback modules and turnouts
        // with feedback, but the address passed is translated as though it
        // is a turnout address.  As a result, we substitute our base
        // address in for the address. after the message is returned.

	return;
	/*
        DCCppMessage msg = DCCppMessage.getFeedbackRequestMsg(mNumber,
                ((mNumber - 1) % 4) < 2);
        synchronized (this) {
            internalState = STATUSREQUESTSENT;
        }
        tc.sendDCCppMessage(msg, null); //status is returned via the manager.
	*/

    }

    synchronized public void setInverted(boolean inverted) {
        if (log.isDebugEnabled()) {
            log.debug("Inverting Turnout State for turnout xt" + mNumber);
        }
        _inverted = inverted;
        if (inverted) {
            _mThrown = jmri.Turnout.CLOSED;
            _mClosed = jmri.Turnout.THROWN;
        } else {
            _mThrown = jmri.Turnout.THROWN;
            _mClosed = jmri.Turnout.CLOSED;
        }
        super.setInverted(inverted);
    }

    public boolean canInvert() {
        return true;
    }

    /**
     * initmessage is a package proteceted class which allows the Manger to send
     * a feedback message at initilization without changing the state of the
     * turnout with respect to whether or not a feedback request was sent. This
     * is used only when the turnout is created by on layout feedback.
     *
     * @param l
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
    synchronized public void message(DCCppReply l) {
        if (log.isDebugEnabled()) {
            log.debug("recieved message: " + l);
        }

        switch (getFeedbackMode()) {
        case EXACT:
            handleExactModeFeedback(l);
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
    public void message(DCCppMessage l) {
    }

    // Handle a timeout notification
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

    // NOTE: DCC++ doesn't do Monitoring mode (yet). But we'll keep the code
    // here in case it does in the future.

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
            if (l.isTurnoutReply() && (l.getTOIDInt() == mNumber)) {
                // This is a feedback message, we need to check and see if it
                // indicates this turnout is to change state or if it is for 
                // another turnout.
		log.debug("Turnout " + mNumber + " MONITORING feedback mode - state change from feedback.");
	    }
        } else if (getCommandedState() != getKnownState()
                || internalState == COMMANDSENT) {
            if (l.isTurnoutReply() && (l.getTOIDInt() == mNumber)) {
		// In Monitoring mode, treat both turnouts with feedback 
		// and turnouts without feedback as turnouts without 
		// feedback.  i.e. just interpret the feedback 
		// message, don't check to see if the motion is complete
		if (parseMonitoringFeedbackMessage(l, 0) != -1) {
		    // We need to tell the turnout to shut off the output.
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
		if (parseMonitoringFeedbackMessage(l, 0) != -1) {
		    // We need to tell the turnout to shut off the output.
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
                synchronized (this) {
                    newCommandedState(_mThrown);
                    newKnownState(getCommandedState());
                }
                return (0);
            } else if (l.getTOIsClosed()) {
                synchronized (this) {
                    newCommandedState(_mClosed);
                    newKnownState(getCommandedState());
                }
                return (0);
            } else {
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

    synchronized private int parseExactFeedbackMessage(DCCppReply l, int startByte) {
        // check validity & addressing
        if (l.getOutputNumInt() == mNumber) {
	    // is for this object, parse the message
	    if (log.isDebugEnabled()) {
                log.debug("Message for turnout " + mNumber);
            }
            if (l.getOutputIsHigh()) {
                synchronized (this) {
                    newCommandedState(_mThrown);
                    newKnownState(getCommandedState());
                }
                return (0);
            } else if (l.getOutputIsLow()) {
                synchronized (this) {
                    newCommandedState(_mClosed);
                    newKnownState(getCommandedState());
                }
                return (0);
            } else {
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
    protected int mNumber;   // XPressNet turnout number
    DCCppTurnoutStateListener _stateListener;  // Internal class object

    private final static Logger log = LoggerFactory.getLogger(DCCppTurnout.class.getName());

}


/* @(#)DCCppTurnout.java */

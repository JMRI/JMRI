package jmri.jmrix.dccpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement a feedback message cache for DCC++ turnouts.
 * <p>
 *
 * @author Paul Bender Copyright (C) 2012
 * @author Mark Underwood Copyright (C) 2015
  *
 * Based on XNetFeedbackMessageCache by Paul Bender
 */
public class DCCppTurnoutReplyCache implements DCCppListener {

    protected DCCppTrafficController tc = null;

    private DCCppReply[] messageCache; // an to hold each of the 512 possible
    // reply messages for the turnouts.

    private Boolean[] messagePending; // hold pending status for each of
    // the possible status request messages.

    // ctor has to register for DCCpp events
    public DCCppTurnoutReplyCache(DCCppTrafficController controller) {
 // TODO: This is likely to be a sparse table. Consider refactoring as
 // a list or something more memory efficient.
        messageCache = new DCCppReply[DCCppConstants.MAX_TURNOUT_ADDRESS];
        for (int i = 0; i < DCCppConstants.MAX_TURNOUT_ADDRESS; i++) {
            messageCache[i] = null;
        }
        messagePending = new Boolean[DCCppConstants.MAX_TURNOUT_ADDRESS];
        for (int i = 0; i < DCCppConstants.MAX_TURNOUT_ADDRESS; i++) {
            messagePending[i] = false;
        }
        tc = controller;
        tc.addDCCppListener(DCCppInterface.FEEDBACK, this);
    }

    // requestCachedStateFromLayout
    // provide any cached state to the turnout.  Otherwise, call the turnout's 
    // requestUpdateFromLayout() method.
    // @param turnout  the DCCppTurnout object we are requesting data for.
    synchronized public void requestCachedStateFromLayout(DCCppTurnout turnout) {
        int pNumber = turnout.getNumber();
        if (messagePending[pNumber]) {
            return;
        }
        try {
            if (messageCache[pNumber] != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Message for turnout " + pNumber + " cached.");
                }
                turnout.message(messageCache[pNumber]);
            } else {
  // TODO: Make sure this doesn't break under a no-feedback model.
                messagePending[pNumber] = true;
                turnout.requestUpdateFromLayout(); // this does nothing. 
            }
        } catch (java.lang.NullPointerException npe) {
     // TODO: Make sure this doesn't break under a no-feedback model.
            messagePending[pNumber] = true;
            turnout.requestUpdateFromLayout();
        }
    }

    // requestCachedStateFromLayout
    // provide any cached state a sensor.  Otherwise, call the sensor's 
    // requestUpdateFromLayout() method.
    // @param sensor  the DCCppSensor object we are requesting data for.
    //
    // TODO: We don't have DCCppSensors yet. May never have them.
    /*
    synchronized public void requestCachedStateFromLayout(DCCppSensor sensor) {
        int pNumber = sensor.getNumber();
        if (messagePending[sensor.getBaseAddress()][sensor.getNibble() >> 4]) {
            return;
        }
        try {
            if (messageCache[sensor.getBaseAddress()][sensor.getNibble() >> 4] != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Message for sensor " + pNumber + " cached.");
                }
                sensor.message(messageCache[sensor.getBaseAddress()][sensor.getNibble() >> 4]);
            } else {
                messagePending[sensor.getBaseAddress()][sensor.getNibble() >> 4] = true;
                sensor.requestUpdateFromLayout();
            }
        } catch (java.lang.NullPointerException npe) {
            messagePending[sensor.getBaseAddress()][sensor.getNibble() >> 4] = true;
            sensor.requestUpdateFromLayout();
        }
    }
    */

    // listen for turnouts, creating them as needed
    @Override
    synchronized public void message(DCCppReply l) {
        if (log.isDebugEnabled()) {
            log.debug("received message: " + l);
        }
        if (l.isTurnoutReply()) {
     // cache the message for later requests
     messageCache[l.getTOIDInt()] = l;
     messagePending[l.getTOIDInt()] = false;
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

    private final static Logger log = LoggerFactory.getLogger(DCCppTurnoutReplyCache.class);

}



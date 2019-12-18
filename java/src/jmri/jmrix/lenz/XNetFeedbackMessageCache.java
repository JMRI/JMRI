package jmri.jmrix.lenz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement a feedback message cache for XpressNet sensors and turnouts.
 *
 * @author Paul Bender Copyright (C) 2012
 */
public class XNetFeedbackMessageCache implements XNetListener {

    protected XNetTrafficController tc = null;

    private XNetReply[][] messageCache; // an to hold each of the 512 possible
    // reply messages for the turnouts.

    private Boolean[][] messagePending; // hold pending status for each of
    // the possible status request messages.

    // ctor has to register for XNet events
    public XNetFeedbackMessageCache(XNetTrafficController controller) {
        messageCache = new XNetReply[256][2];
        for (int i = 0; i < 256; i++) {
            messageCache[i][0] = messageCache[i][1] = null;
        }
        messagePending = new Boolean[256][2];
        for (int i = 0; i < 256; i++) {
            messagePending[i][0] = messagePending[i][1] = false;
        }
        tc = controller;
        tc.addXNetListener(XNetInterface.FEEDBACK, this);
    }

    // requestCachedStateFromLayout
    // provide any cached state to the turnout.  Otherwise, call the turnout's 
    // requestUpdateFromLayout() method.
    // @param turnout  the XNetTurnout object we are requesting data for.
    synchronized public void requestCachedStateFromLayout(XNetTurnout turnout) {
        int pNumber = turnout.getNumber();
        if (messagePending[(pNumber - 1) / 4][((pNumber - 1) % 4) < 2 ? 0 : 1]) {
            return;
        }
        try {
            if (messageCache[(pNumber - 1) / 4][((pNumber - 1) % 4) < 2 ? 0 : 1] != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Message for turnout " + pNumber + " cached.");
                }
                turnout.message(messageCache[(pNumber - 1) / 4][((pNumber - 1) % 4) < 2 ? 0 : 1]);
            } else {
                messagePending[(pNumber - 1) / 4][((pNumber - 1) % 4) < 2 ? 0 : 1] = true;
                turnout.requestUpdateFromLayout();
            }
        } catch (java.lang.NullPointerException npe) {
            messagePending[(pNumber - 1) / 4][((pNumber - 1) % 4) < 2 ? 0 : 1] = true;
            turnout.requestUpdateFromLayout();
        }
    }

    /**
     * Provide any cached state a sensor. Otherwise, call the sensor's
     * requestUpdateFromLayout() method.
     *
     * @param sensor the XNetSensor object we are requesting data for
     */
    synchronized public void requestCachedStateFromLayout(XNetSensor sensor) {
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

    /**
     * Listen for turnouts, creating them as needed.
     */
    @Override
    synchronized public void message(XNetReply l) {
        if (log.isDebugEnabled()) {
            log.debug("received message: " + l);
        }
        if (l.isFeedbackBroadcastMessage()) {
            int numDataBytes = l.getElement(0) & 0x0f;
            for (int i = 1; i < numDataBytes; i += 2) {
                // cache the message for later requests
                messageCache[l.getElement(1)][(l.getElement(2) & 0x10) >> 4] = l;
                messagePending[l.getElement(1)][(l.getElement(2) & 0x10) >> 4] = false;
            }
        }
    }

    /**
     * Listen for the messages to the LI100/LI101.
     */
    @Override
    public void message(XNetMessage l) {
    }

    /**
     * Handle a timeout notification.
     */
    @Override
    public void notifyTimeout(XNetMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(XNetFeedbackMessageCache.class);

}



package jmri.jmrix.lenz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement a feedback message cache for XpressNet sensors and turnouts.
 *
 * @author Paul Bender Copyright (C) 2012
 */
public class XNetFeedbackMessageCache implements XNetListener {

    protected XNetTrafficController tc;

    private final XNetReply[] messageCache = new XNetReply[512]; // an to hold each of the 512 possible
    // reply messages for the turnouts.

    private final byte[] messagePending = new byte[512 / 8]; // hold pending status for each of
    // the possible status request messages (bitfield)

    // ctor has to register for XNet events
    public XNetFeedbackMessageCache(XNetTrafficController controller) {
        tc = controller;
        tc.addXNetListener(XNetInterface.FEEDBACK, this);
    }

    // requestCachedStateFromLayout
    // provide any cached state to the turnout.  Otherwise, call the turnout's 
    // requestUpdateFromLayout() method.
    // @param turnout  the XNetTurnout object we are requesting data for.
    public void requestCachedStateFromLayout(XNetTurnout turnout) {
        int pNumber = turnout.getNumber();
        log.debug("asking for cached feedback for turnout {}.",pNumber);
        pNumber--;
        if (requestCachedState(2, pNumber, turnout)) {
            turnout.requestUpdateFromLayout();
        }
    }

    /**
     * Provide any cached state a sensor. Otherwise, call the sensor's
     * requestUpdateFromLayout() method.
     *
     * @param sensor the XNetSensor object we are requesting data for
     */
    public synchronized void requestCachedStateFromLayout(XNetSensor sensor) {
        int pNumber = sensor.getNumber();
        log.debug("asking for cached feedback for sensor {}.",pNumber);
        pNumber--;
        if (requestCachedState(4, pNumber, sensor)) {
            sensor.requestUpdateFromLayout();
        }
    }
    
    private boolean requestCachedState(int statesPerNibble, int pNumber, XNetListener target) {
        int replyIndex = pNumber / statesPerNibble;
        int bitIdx = replyIndex / 8;
        int bitMask = pNumber % 8;
        XNetReply cached;
        
        // do not extend the lock to code execution:
        synchronized (this) {
            if ((messagePending[bitIdx] & (1 << (bitMask))) > 0) {
                return false;
            }
             cached = messageCache[replyIndex];
             if (cached == null) {
                messagePending[bitIdx] |= (1 << bitMask);
             }
        }
        if (cached != null) {
            target.message(cached);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Listen for turnouts, creating them as needed.
     */
    @Override
    public synchronized void message(XNetReply l) {
        if (log.isDebugEnabled()) {
            log.debug("received message: {}",l);
        }
        if (!l.isFeedbackBroadcastMessage()) {
            return;
        }
        int numDataBytes = l.getElement(0) & 0x0f;
        for (int i = 1; i < numDataBytes; i += 2) {
            // cache the message for later requests
            int nibbleIndex = l.getElement(i) * 2 + (l.getElement(i + 1) & 0x10) >> 4;
            messageCache[nibbleIndex] = l;
        }
    }

    /**
     * Listen for the messages to the LI100/LI101.
     */
    @Override
    public void message(XNetMessage l) {
        // outgoing messages are not currently used
    }

    /**
     * Handle a timeout notification.
     */
    @Override
    public void notifyTimeout(XNetMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message {}",msg);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(XNetFeedbackMessageCache.class);

}



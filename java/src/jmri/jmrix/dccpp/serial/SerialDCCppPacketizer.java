/**
 * SerialDCCppPacketizer.java
 */
package jmri.jmrix.dccpp.serial;

import java.util.concurrent.DelayQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.dccpp.DCCppListener;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppPacketizer;
import jmri.util.ThreadingUtil;

/**
 * This is an extension of the DCCppPacketizer to handle the device specific
 * requirements of the DCC++.
 * <p>
 * In particular, SerialDCCppPacketizer adds functions to add and remove the
 * {@literal "<" and ">"} bytes that appear around any message read in.
 *
 * Note that the bracket-adding could be pushed up to DCCppPacketizer, as it is
 * a protocol thing, not an interface implementation thing. We'll come back to
 * that later.
 *
 * What is however interface specific is the background refresh of functions.
 * DCC++ sends the DCC commands exactly once. A background thread will
 * repeat the last seen function commands to compensate for any momentary
 * power loss or to recover from power off / power on events. It only makes
 * sense to do this on the actual serial interface as it will be transparent for
 * the
 * network clients.
 *
 * @author Paul Bender Copyright (C) 2005
 * @author Mark Underwood Copyright (C) 2015
 * @author Costin Grigoras Copyright (C) 2018
 *
 *         Based on LIUSBXNetPacketizer by Paul Bender
 */
public class SerialDCCppPacketizer extends DCCppPacketizer {

    final DelayQueue<DCCppMessage> resendFunctions = new DelayQueue<>();

    boolean activeBackgroundRefresh = true;

    public SerialDCCppPacketizer(final jmri.jmrix.dccpp.DCCppCommandStation pCommandStation) {
        super(pCommandStation);
        log.debug("Loading Serial Extention to DCCppPacketizer");
    }

    /**
     * Determine how many bytes the entire message will take, including
     * space for header and trailer
     *
     * @param m The message to be sent
     * @return Number of bytes
     */
    @Override
    protected int lengthOfByteStream(final jmri.jmrix.AbstractMRMessage m) {
        return m.getNumDataElements() + 2;
    }

    /**
     * <code>true</code> when the self-rescheduling function refresh action was
     * initially queued, to avoid duplicate actions
     */
    private boolean backgroundRefreshStarted = false;

    final class RefreshAction implements ThreadingUtil.ThreadAction {
        @Override
        public void run() {
            try {
                if (activeBackgroundRefresh) {
                    final DCCppMessage message = resendFunctions.poll();

                    if (message != null) {
                        message.setRetries(0);
                        sendDCCppMessage(message, null);
                    }
                }
            } finally {
                ThreadingUtil.runOnLayoutDelayed(this, 250);
            }
        }
    }

    private void enqueueFunction(final DCCppMessage m) {
        /**
         * Set again the same group function value 250ms later (or more,
         * depending on the queue depth)
         */
        m.delayFor(250);
        resendFunctions.offer(m);

        synchronized (this) {
            if (!backgroundRefreshStarted) {
                ThreadingUtil.runOnLayoutDelayed(new RefreshAction(), 250);
                backgroundRefreshStarted = true;
            }
        }
    }

    @Override
    public void sendDCCppMessage(final DCCppMessage m, final DCCppListener reply) {
        final boolean isFunction = m.isFunctionMessage();

        /**
         * Remove a previous value for the same function (DCC address + function
         * group) based on
         * {@link jmri.jmrix.dccpp.DCCppMessage#equals(DCCppMessage)}
         */
        if (isFunction)
            resendFunctions.remove(m);

        super.sendDCCppMessage(m, reply);

        if (isFunction)
            enqueueFunction(m);
    }

    /**
     * Clear the background refresh queue. The state is still kept in JMRI.
     */
    public void clearRefreshQueue() {
        resendFunctions.clear();
    }

    /**
     * Check how many entries are in the background refresh queue
     *
     * @return number of queued function groups
     */
    public int getQueueLength() {
        return resendFunctions.size();
    }

    /**
     * Enable or disable the background refresh thread
     *
     * @param activeState <code>true</code> to keep refreshing the functions,
     *            <code>false</code> to disable this functionality.
     * @return the previous active state of the background refresh thread
     */
    public boolean setActiveRefresh(final boolean activeState) {
        final boolean oldActiveState = activeBackgroundRefresh;

        activeBackgroundRefresh = activeState;

        return oldActiveState;
    }

    /**
     * Check if the background function refresh thread is active or not
     *
     * @return the background refresh status, <code>true</code> for active,
     *         <code>false</code> if disabled.
     */
    public boolean isActiveRefresh() {
        return activeBackgroundRefresh;
    }

    private static final Logger log = LoggerFactory.getLogger(SerialDCCppPacketizer.class);
}

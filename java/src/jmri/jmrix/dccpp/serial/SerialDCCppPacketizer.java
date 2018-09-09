/**
 * SerialDCCppPacketizer.java
 */
package jmri.jmrix.dccpp.serial;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.dccpp.DCCppListener;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppPacketizer;

/**
 * This is an extension of the DCCppPacketizer to handle the device specific
 * requirements of the DCC++.
 * <P>
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
public final class SerialDCCppPacketizer extends DCCppPacketizer {
    
    @SuppressFBWarnings(value = "SC_START_IN_CTOR", justification = "with existing code structure, we do not expect this to ever be subclassed.")

    final DelayQueue<DCCppMessage> resendFunctions = new DelayQueue<>();

    public SerialDCCppPacketizer(final jmri.jmrix.dccpp.DCCppCommandStation pCommandStation) {
        super(pCommandStation);
        log.debug("Loading Serial Extention to DCCppPacketizer");

        // this is the background thread that periodically refreshes the last
        // known function settings
        final Thread backgroundRefresh = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        final DCCppMessage message = resendFunctions.poll(1, TimeUnit.MINUTES);

                        if (message != null) {
                            message.setRetries(1);
                            sendDCCppMessage(message, null);

                            // At 115200 baud only ~1k messages/s can be sent.
                            // Be nice and don't overload the wire.
                            sleep(1);
                        }

                        setName("SerialDCCppPacketizer.bkg_refresh (" + resendFunctions.size() + " msg)");
                    } catch (@SuppressWarnings("unused") final InterruptedException e) {
                        // should exit if interrupted
                    }
                }
            }
        };

        backgroundRefresh.setDaemon(true);
        backgroundRefresh.start();
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
        final int len = m.getNumDataElements() + 2;
        return len;
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

        if (isFunction) {
            /**
             * Set again the same group function value 250ms later (or more,
             * depending on the queue depth; limited to 1kHz of calls)
             */
            m.delayFor(250);
            resendFunctions.offer(m);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialDCCppPacketizer.class);
}

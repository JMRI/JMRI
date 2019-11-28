package jmri.jmrix.lenz.liusbserver;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.lenz.XNetPacketizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an extension of the XNetPacketizer to handle the device specific
 * requirements of the LIUSB Server.
 * <p>
 * In particular, LIUSBServerXNetPacketizer counts the number of commands
 * received.
 *
 * @author Paul Bender, Copyright (C) 2009
 *
 */
public class LIUSBServerXNetPacketizer extends XNetPacketizer {

    public LIUSBServerXNetPacketizer(jmri.jmrix.lenz.LenzCommandStation pCommandStation) {
        super(pCommandStation);
        log.debug("Loading LIUSB Server Extension to XNetPacketizer");
    }

    /**
     * Actually transmits the next message to the port
     */
    @SuppressFBWarnings(value = {"UW_UNCOND_WAIT"},
             justification = "Wait is for external hardware, which doesn't necessarilly respond, to process the data.")

    @Override
    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        log.debug("forwardToPort message: [{}]", m);
        // remember who sent this
        mLastSender = reply;

        // forward the message to the registered recipients,
        // which includes the communications monitor, except the sender.
        // Schedule notification via the Swing event queue to ensure order
        Runnable r = new XmtNotifier(m, mLastSender, this);
        javax.swing.SwingUtilities.invokeLater(r);

        // stream the bytes
        try {
            if (ostream != null) {
                while (m.getRetries() >= 0) {
                    if (portReadyToSend(controller)) {
                        ostream.write((m + "\n\r").getBytes(java.nio.charset.Charset.forName("UTF-8")));
                        ostream.flush();
                        log.debug("written");
                        break;
                    } else if (m.getRetries() >= 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("Retry message: {} attempts remaining: {}", m.toString(), m.getRetries());
                        }
                        m.setRetries(m.getRetries() - 1);
                        try {
                            synchronized (xmtRunnable) {
                                xmtRunnable.wait(m.getTimeout());
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // retain if needed later
                            log.error("retry wait interrupted");
                        }
                    } else {
                        log.warn("sendMessage: port not ready for data sending: {}", m.toString());
                    }
                }
            } else {  // ostream is null
                // no stream connected
                connectionWarn();
            }
        } catch (java.io.IOException e) {
            // start the recovery process if an exception occurs.
            portWarn(e);
            controller.recover();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LIUSBServerXNetPacketizer.class);

}

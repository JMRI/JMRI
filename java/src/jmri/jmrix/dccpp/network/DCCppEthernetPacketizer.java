package jmri.jmrix.dccpp.network;
    
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import javax.swing.SwingUtilities;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of the DCCppPacketizer to handle the device specific
 * requirements of the DCC++ Ethernet.
 * <p>
 * In particular, DCCppEthernetPacketizer counts the number of commands
 * received.
 *
 * Based on LIUSBEthernetXnetPacketizer
 * 
 * @author Paul Bender, Copyright (C) 2011
 * @author      Mark Underwood, Copyright (C) 2015
 */
public class DCCppEthernetPacketizer extends jmri.jmrix.dccpp.serial.SerialDCCppPacketizer {

    public DCCppEthernetPacketizer(jmri.jmrix.dccpp.DCCppCommandStation pCommandStation) {
        super(pCommandStation);
        log.debug("Loading DCC++ Ethernet Extension to DCCppPacketizer");
    }

    /**
     * Actually transmits the next message to the port
     */
    // NOTE: This is (for now) an EXACT copy of the content of AbstractMRTrafficController.forwardToPort()
    // except for adding the call to controller.recover() at the bottom in the "catch"
    @Override
    @SuppressFBWarnings(value = {"TLW_TWO_LOCK_WAIT"},justification = "Two locks needed for synchronization here, this is OK")
    synchronized protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        log.debug("forwardToPort message: [{}]", m);
        // remember who sent this
        mLastSender = reply;
        
        // forward the message to the registered recipients,
        // which includes the communications monitor, except the sender.
        // Schedule notification via the Swing event queue to ensure order
        Runnable r = new XmtNotifier(m, mLastSender, this);
        SwingUtilities.invokeLater(r);
        
        // stream to port in single write, as that's needed by serial
        byte msg[] = new byte[lengthOfByteStream(m)];
        // add header
        int offset = addHeaderToOutput(msg, m);
        
        // add data content
        int len = m.getNumDataElements();
        for (int i = 0; i < len; i++) {
            msg[i + offset] = (byte) m.getElement(i);
        }
        // add trailer
        addTrailerToOutput(msg, len + offset, m);
        // and stream the bytes
        try {
            if (ostream != null) {
                if (log.isDebugEnabled()) {
                    StringBuilder f = new StringBuilder("formatted message: ");
                    for (int i = 0; i < msg.length; i++) {
                        f.append(Integer.toHexString(0xFF & msg[i]));
                        f.append(" ");
                    }
                    log.debug(f.toString());
                }
                while (m.getRetries() >= 0) {
                    if (portReadyToSend(controller)) {
                        ostream.write(msg);
                        ostream.flush();
                        log.debug("written, msg timeout: {} mSec", m.getTimeout());
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
                        log.warn("sendMessage: port not ready for data sending: {}", Arrays.toString(msg));
                    }
                }
            } else {  // ostream is null
                // no stream connected
                connectionWarn();
            }
        } catch (java.io.IOException e) {
            // TODO Currently there's no port recovery if an exception occurs
            // must restart JMRI to clear xmtException.
            //xmtException = true;
            portWarn(e);
            // Attempt to recover the connection...
            controller.recover();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppEthernetPacketizer.class);

}

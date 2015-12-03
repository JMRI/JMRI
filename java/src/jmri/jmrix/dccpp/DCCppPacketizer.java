/**
 * DCCppPacketizer.java
 */
package jmri.jmrix.dccpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;

/**
 * Converts Stream-based I/O to/from DCC++ messages. The "DCCppInterface" side
 * sends/receives DCCppMessage objects. The connection to a DCCppPortController is
 * via a pair of *Streams, which then carry sequences of characters for
 * transmission.
 * <P>
 * Messages come to this via the main GUI thread, and are forwarded back to
 * listeners in that same thread. Reception and transmission are handled in
 * dedicated threads by RcvHandler and XmtHandler objects. Those are internal
 * classes defined here. The thread priorities are:
 * <P>
 * <UL>
 * <LI> RcvHandler - at highest available priority
 * <LI> XmtHandler - down one, which is assumed to be above the GUI
 * <LI> (everything else)
 * </UL>
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @author	Mark Underwood Copyright (C) 2015
 * @version $Revision$
 *
 * Based on XNetPacketizer by Bob Jacobsen
 *
 */
public class DCCppPacketizer extends DCCppTrafficController {

    public DCCppPacketizer(DCCppCommandStation pCommandStation) {
        super(pCommandStation);
        // The instance method (from DCCppTrafficController) is deprecated
        // But for the moment we need to make sure we set the static
        // self variable, and the instance method does this for us in a
        // static way (which makes findbugs happy).
        //instance();
	log.debug("DCCppPacketizer created.");
    }

// The methods to implement the DCCppInterface
    public boolean status() {
        return (ostream != null & istream != null);
    }

    /**
     * Forward a preformatted DCCppMessage to the actual interface.
     *
     *The message is converted to a byte array and queue for transmission
     *
     * @param m Message to send;
     */
    public void sendDCCppMessage(DCCppMessage m, DCCppListener reply) {
        if (m.length() != 0) {
            sendMessage(m, reply);
            java.lang.Thread.yield();
        }
    }

    /**
     * Add header to the outgoing byte stream.
     *
     * @param msg The output byte stream
     * @return next location in the stream to fill
     */
    protected int addHeaderToOutput(byte[] msg, jmri.jmrix.AbstractMRMessage m) {
        if (log.isDebugEnabled()) {
            log.debug("Appending '<' to start of outgoing message. msg length = {}", msg.length);
        }
        msg[0] = (byte) '<';
        return 1;
    }

    //    public void startThreads() {
	// Doesn't do anything generically.
	// Most Packetizers won't do anything.  The TCP
    //}


    /**
     * Add trailer to the outgoing byte stream. This version adds the checksum
     * to the last byte.
     *
     * @param msg    The output byte stream
     * @param offset the first byte not yet used
     */
    @Override
    protected void addTrailerToOutput(byte[] msg, int offset, jmri.jmrix.AbstractMRMessage m) {
	log.debug("aTTO offset = {} message = {} msg length = {}", offset, m.toString(), msg.length);
        if (m.getNumDataElements() == 0) {
            return;
        }
        //msg[offset - 1] = (byte) m.getElement(m.getNumDataElements() - 1);
	msg[offset] = '>';
	log.debug("finished string = {}", new String(msg, StandardCharsets.UTF_8));
    }

    /**
     * Check to see if PortController object can be sent to. returns true if
     * ready, false otherwise May throw an Exception.
     */
    @Override
    public boolean portReadyToSend(jmri.jmrix.AbstractPortController p) throws Exception {
        if (((DCCppPortController) p).okToSend()) {
            ((DCCppPortController) p).setOutputBufferEmpty(false);
            return true;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("DCC++ port not ready to receive");
            }
            return false;
        }
    }

    /**
     * Get characters from the input source, and file a message.
     * <P>
     * Returns only when the message is complete.
     * <P>
     * Only used in the Receive thread.
     *
     * @returns filled message
     * @throws IOException when presented by the input source.
     */
    private DCCppMessage loadChars() throws java.io.IOException {
	// Spin waiting for start-of-frame '<' character (and toss it)
	String s = new String();
	byte char1;
	boolean found_start = false;

	while (!found_start) {
	    char1 = readByteProtected(istream);
	    if ((char1 & 0xFF) == '<') {
		found_start = true;
		log.debug("Found starting < ");
		break; // A bit redundant with setting the loop condition true (false)
	    } else {
		char1 = readByteProtected(istream);
	    }
	}
	// Now, suck in the rest of the message...
        for (int i = 0; i < DCCppConstants.MAX_MESSAGE_SIZE; i++) {
            char1 = readByteProtected(istream);
	    if (char1 == '>') {
		log.debug("msg found > ");
		// Don't store the >
		break;
	    } else {
		log.debug("msg read byte {}", char1);
		char c = (char) (char1 & 0x00FF);
		s += Character.toString(c);
	    }
	}
	// TODO: Still need to strip leading and trailing whitespace.
	log.debug("Complete message = {}", s);
	return(DCCppMessage.parseDCCppMessage(s));
    }

    static Logger log = LoggerFactory.getLogger(DCCppPacketizer.class.getName());
}

/* @(#)DCCppPacketizer.java */

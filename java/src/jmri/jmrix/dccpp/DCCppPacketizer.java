package jmri.jmrix.dccpp;

import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from DCC-EX messages. The "DCCppInterface" side
 * sends/receives DCCppMessage objects. The connection to a DCCppPortController
 * is via a pair of *Streams, which then carry sequences of characters for
 * transmission.
 * <p>
 * Messages come to this via the main GUI thread, and are forwarded back to
 * listeners in that same thread. Reception and transmission are handled in
 * dedicated threads by RcvHandler and XmtHandler objects. Those are internal
 * classes defined here. The thread priorities are:
 * <ul>
 * <li> RcvHandler - at highest available priority
 * <li> XmtHandler - down one, which is assumed to be above the GUI
 * <li> (everything else)
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Mark Underwood Copyright (C) 2015
 *
 * Based on XNetPacketizer by Bob Jacobsen
 */
public class DCCppPacketizer extends DCCppTrafficController {

    public DCCppPacketizer(DCCppCommandStation pCommandStation) {
        super(pCommandStation);
        // The instance method (from DCCppTrafficController) is deprecated.
        // But for the moment we need to make sure we set the static
        // self variable, and the instance method does this for us in a
        // static way (which makes spotbugs happy).
        //instance();
        log.debug("DCCppPacketizer created");
    }

// The methods to implement the DCCppInterface

    /**
     * Forward a preformatted DCCppMessage to the actual interface.
     *
     * The message is converted to a byte array and queue for transmission
     *
     * @param m     Message to send
     * @param reply Listener to notify when the reply to the message is received
     */
    //TODO: Can this method be folded back up into the parent
    // DCCppTrafficController class?
    @Override
    public void sendDCCppMessage(DCCppMessage m, DCCppListener reply) {
        if (m.length() != 0) {
            log.debug("Adding '{}' to send queue", m);            
            sendMessage(m, reply);
            // why the next line?
            // https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html#yield--
            // states "It is rarely appropriate to use this method"
            java.lang.Thread.yield();
        }
    }

    /**
     * Add header to the outgoing byte stream.
     *
     * @param msg The output byte stream
     * @param m   ignored
     * @return next location in the stream to fill
     */
    //TODO: Can this method be folded back up into the parent
    // DCCppTrafficController class?
    @Override
    protected int addHeaderToOutput(byte[] msg, jmri.jmrix.AbstractMRMessage m) {
        if (log.isTraceEnabled()) {
            log.trace("Appending '<' to start of outgoing message. msg length = {}", msg.length);
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
     * @param m      the message to check
     */
    //TODO: Can this method be folded back up into the parent
    // DCCppTrafficController class?
    @Override
    protected void addTrailerToOutput(byte[] msg, int offset, jmri.jmrix.AbstractMRMessage m) {
        if (log.isTraceEnabled()) {
            log.trace("aTTO offset = {} message = {} msg length = {}", offset, m, msg.length);
        }
        if (m.getNumDataElements() == 0) {
            return;
        }
        //msg[offset - 1] = (byte) m.getElement(m.getNumDataElements() - 1);
        msg[offset] = '>';
        if (log.isTraceEnabled()) {
            log.trace("finished string = {}", new String(msg, StandardCharsets.UTF_8));
        }
    }

    /**
     * Check to see if PortController object can be sent to. returns true if
     * ready, false otherwise May throw an Exception.
     */
    @Override
    public boolean portReadyToSend(jmri.jmrix.AbstractPortController p) {
        if (p instanceof DCCppPortController && ((DCCppPortController) p).okToSend()) {
            ((DCCppPortController) p).setOutputBufferEmpty(false);
            return true;
        } else {
            log.warn("DCC-EX port not ready to send");
            return false;
        }
    }

    /**
     * Get characters from the input source, and file a message.
     * <p>
     * Returns only when the message is complete.
     * <p>
     * Only used in the Receive thread.
     *
     * @param msg     message to fill
     * @param istream character source.
     * @throws java.io.IOException when presented by the input source.
     */
    // TODO: Can this method be folded back up into the parent DCCppTrafficController class?
    @Override
    protected void loadChars(jmri.jmrix.AbstractMRReply msg, java.io.DataInputStream istream) throws java.io.IOException {
        log.trace("loading characters from port");
        if (!(msg instanceof DCCppReply)) {
            log.error("loadChars called on non-DCCppReply msg!");
            return;
        }
        byte char1 = readByteProtected(istream);
        while (char1 != '<') {
            char1 = readByteProtected(istream);
            if (char1 != '<') log.trace("skipping char: ({})", (char) char1);
        }
        log.trace("Message started");
        String body = readFrameBody(istream, msg.maxSize());
        log.debug("Received: '{}'", body);
        ((DCCppReply) msg).parseReply(body);
    }

    /** Reads one frame body after {@code <}: broadcast ({@code <* *>}) or regular up to {@code >}. */
    String readFrameBody(java.io.DataInputStream istream, int maxSize) throws java.io.IOException {
        StringBuilder body = new StringBuilder();
        byte ch = readByteProtected(istream);
        body.append((char) ch);
        boolean broadcast = (ch == '*');
        boolean prevStar = broadcast;
        boolean inQuotes = false;

        while (body.length() < maxSize) {
            ch = readByteProtected(istream);
            if (broadcast) {
                if (prevStar && ch == '>') {
                    return body.toString(); // drop > only; trailing * stays in body for parseDCCppReply
                }
                prevStar = (ch == '*');
            } else {
                if (ch == '"') inQuotes = !inQuotes;
                else if (ch == '>' && !inQuotes) return body.toString();
            }
            body.append((char) ch);
        }
        log.warn("msg size {} exceeded before end of msg encountered.", maxSize);
        return body.toString();
    }

    private static final Logger log = LoggerFactory.getLogger(DCCppPacketizer.class);

}

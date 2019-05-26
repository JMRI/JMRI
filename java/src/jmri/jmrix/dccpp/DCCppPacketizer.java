package jmri.jmrix.dccpp;

import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from DCC++ messages. The "DCCppInterface" side
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
     * @param m      the message to check
     */
    //TODO: Can this method be folded back up into the parent
    // DCCppTrafficController class?
    @Override
    protected void addTrailerToOutput(byte[] msg, int offset, jmri.jmrix.AbstractMRMessage m) {
        log.debug("aTTO offset = {} message = {} msg length = {}", offset, m, msg.length);
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
    public boolean portReadyToSend(jmri.jmrix.AbstractPortController p) {
        if (p != null && ((DCCppPortController) p).okToSend()) {
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
     * <p>
     * Returns only when the message is complete.
     * <p>
     * Only used in the Receive thread.
     *
     * @param msg     message to fill
     * @param istream character source.
     * @throws java.io.IOException when presented by the input source.
     */
    //TODO: Can this method be folded back up into the parent
    // DCCppTrafficController class?
    @Override
    protected void loadChars(jmri.jmrix.AbstractMRReply msg, java.io.DataInputStream istream) throws java.io.IOException {
        int i;
        StringBuilder m = new StringBuilder("");
        if (log.isDebugEnabled()) {
            log.debug("loading characters from port");
        }

        if (!(msg instanceof DCCppReply)) {
            log.error("SerialDCCppPacketizer.loadChars called on non-DCCppReply msg!");
            return;
        }

        byte char1 = readByteProtected(istream);
        while (char1 != '<') {
            // Spin waiting for '<'
            char1 = readByteProtected(istream);
        }
        log.debug("Serial: Message started...");
        // Pick up the rest of the command
        for (i = 0; i < msg.maxSize(); i++) {
            char1 = readByteProtected(istream);
            if (char1 == '>') {
                log.debug("Received: {}", m);
                // NOTE: Cast is OK because we checked runtime type of msg above.
                ((DCCppReply) msg).parseReply(m.toString());
                break;
            } else {
                m.append(Character.toString((char) char1));
                //char1 = readByteProtected(istream);
                log.debug("msg char[{}]: {} ({})", i, char1, Character.toString((char) char1));
                //msg.setElement(i, char1 & 0xFF);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppPacketizer.class);

}

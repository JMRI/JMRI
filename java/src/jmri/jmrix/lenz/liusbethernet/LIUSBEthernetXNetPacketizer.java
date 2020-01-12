package jmri.jmrix.lenz.liusbethernet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an extension of the XNetPacketizer to handle the device specific
 * requirements of the LIUSBEthernet.
 * <p>
 * In particular, LIUSBEthernetXNetPacketizer counts the number of commands
 * received.
 *
 * @author Paul Bender, Copyright (C) 2011
 *
 */
public class LIUSBEthernetXNetPacketizer extends jmri.jmrix.lenz.liusb.LIUSBXNetPacketizer {

    public LIUSBEthernetXNetPacketizer(jmri.jmrix.lenz.LenzCommandStation pCommandStation) {
        super(pCommandStation);
        log.debug("Loading LIUSB Ethernet Extension to XNetPacketizer");
    }

    /**
     * Determine how much many bytes the entire message will take, including
     * space for header and trailer.
     *
     * @param m the message to be sent
     * @return number of bytes
     */
    @Override
    protected int lengthOfByteStream(jmri.jmrix.AbstractMRMessage m) {
        int len = m.getNumDataElements() + 2;
        int cr = 0;
        if (!m.isBinary()) {
            cr = 1;  // space for return
        }
        return len + cr;
    }

    /**
     * Add header to the outgoing byte stream.
     *
     * @param msg the output byte stream
     * @return next location in the stream to fill
     */
    @Override
    protected int addHeaderToOutput(byte[] msg, jmri.jmrix.AbstractMRMessage m) {
        if (log.isDebugEnabled()) {
            log.debug("Appending 0xFF 0xFE to start of outgoing message");
        }
        msg[0] = (byte) 0xFF;
        msg[1] = (byte) 0xFE;
        return 2;
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
    @Override
    protected void loadChars(jmri.jmrix.AbstractMRReply msg, java.io.DataInputStream istream) throws java.io.IOException {
        int i;
        byte lastbyte = (byte) 0xFF;
        log.debug("loading characters from port");
        for (i = 0; i < msg.maxSize(); i++) {
            byte char1 = readByteProtected(istream);
            // This is a test for the LIUSB device
            while ((i == 0) && ((char1 & 0xF0) == 0xF0)) {
                if ((char1 & 0xFF) != 0xF0 && (char1 & 0xFF) != 0xF2) {
                    // save this so we can check for unsolicited
                    // messages.
                    lastbyte = char1;
                    //  toss this byte and read the next one
                    char1 = readByteProtected(istream);
                }

            }
            // LIUSB messages are preceeded by 0xFF 0xFE if they are
            // responses to messages we sent.  If they are unrequested
            // information, they are preceeded by 0xFF 0xFD.
            if (lastbyte == (byte) 0xFD) {
                msg.setUnsolicited();
            }
            msg.setElement(i, char1 & 0xFF);
            if (endOfMessage(msg)) {
                break;
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LIUSBEthernetXNetPacketizer.class);

}

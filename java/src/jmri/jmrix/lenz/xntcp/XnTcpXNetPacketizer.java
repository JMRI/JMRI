package jmri.jmrix.lenz.xntcp;

import jmri.jmrix.lenz.XNetPacketizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an extension of the XNetPacketizer to handle the device specific
 * requirements of the XnTcp.
 * <p>
 * In particular, XnTcpXNetPacketizer counts the number of commands received.
 *
 * @author Giorgio Terdina Copyright (C) 2008-2011, based on LIUSB packetizer by
 * Paul Bender, Copyright (C) 2005
 *  GT - May 2011 - Removed calls to deprecated
 * method "XnTcpAdapter.instance()"
 *
 */
public class XnTcpXNetPacketizer extends XNetPacketizer {

    public XnTcpXNetPacketizer(jmri.jmrix.lenz.LenzCommandStation pCommandStation) {
        super(pCommandStation);
        log.debug("Loading XnTcp Extension to XNetPacketizer");
    }

    /**
     * Get characters from the input source, and fill a message.
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
        int i, char1;
        i = 0;
        try {
            // Make sure we don't overwrite output buffer
            while (i < msg.maxSize()) {
                // Read a byte
                char1 = istream.read();
                // Was the communication closed by the XpressNet/Tcp interface, or lost?
                if (char1 < 0) {
                    // We cannot communicate anymore!
                    ((XnTcpAdapter) controller).xnTcpError();
                    throw new java.io.EOFException("Lost communication with XnTcp interface");
                }
                // Store the byte.
                msg.setElement(i++, (byte) char1 & 0xFF);
                log.debug("XnTcpNetPacketizer: received {}", Integer.toHexString(char1 & 0xff));
                // If the XpressNet packet is completed, exit the loop
                if (endOfMessage(msg)) {
                    break;
                }
            }
            // Packet received
            // Assume that the last packet sent was acknowledged, either by the Command Station,
            // either by the interface itself.

            ((XnTcpAdapter) controller).xnTcpSetPendingPackets(-1);
            log.debug("XnTcpNetPacketizer: received end of packet");
        } catch (java.io.InterruptedIOException ex) {
            return;
        } catch (java.io.IOException ex) {
            ((XnTcpAdapter) controller).xnTcpError();
            throw ex;
        }

    }

    private final static Logger log = LoggerFactory.getLogger(XnTcpXNetPacketizer.class);

}

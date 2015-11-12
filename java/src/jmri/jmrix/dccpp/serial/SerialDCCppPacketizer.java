/**
 * SerialDCCppPacketizer.java
 */
package jmri.jmrix.dccpp.serial;

import jmri.jmrix.dccpp.DCCppPacketizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an extention of the DCCppPacketizer to handle the device specific
 * requirements of the DCC++.
 * <P>
 * In particular, SerialDCCppPacketizer adds functions to add and remove the "<" and ">"
 * bytes that appear around any message read in.
 *
 * Note that the bracket-adding could be pushed up to DCCppPacketizer, as it is a protocol
 * thing, not an interface implementation thing.  We'll come back to that later. 
 *
 * @author	Paul Bender Copyright (C) 2005
 * @author	Mark Underwood Copyright (C) 2015
 * @version $Revision$
 *
 * Based on LIUSBXNetPacketizer by Paul Bender
 */
public class SerialDCCppPacketizer extends DCCppPacketizer {

    public SerialDCCppPacketizer(jmri.jmrix.dccpp.DCCppCommandStation pCommandStation) {
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
    protected int lengthOfByteStream(jmri.jmrix.AbstractMRMessage m) {
        int len = m.getNumDataElements() + 2;
        int cr = 0;
        if (!m.isBinary()) { // TODO: Hmm... need to understand this isBinary stuff better...
            cr = 1;  // space for return
        }
        //return len + cr;
        return len;
    }

    /**
     * Get characters from the input source, and file a message.
     * <P>
     * Returns only when the message is complete.
     * <P>
     * Only used in the Receive thread.
     *
     * @param msg     message to fill
     * @param istream character source.
     * @throws java.io.IOException when presented by the input source.
     */
    //@Override
    protected void loadChars(jmri.jmrix.AbstractMRReply msg, java.io.DataInputStream istream) throws java.io.IOException {
        int i;
        byte lastbyte = (byte) 0xFF;
        if (log.isDebugEnabled()) {
            log.debug("loading characters from port");
        }
        for (i = 0; i < msg.maxSize(); i++) {
            byte char1 = readByteProtected(istream);
	    // Spin waiting for the start-of-frame '<' character
            while ((i == 0)) {
                if ((char1 & 0xFF) != '<') {
                    // save this so we can check for unsolicited
                    // messages. ( TODO: Not needed for DCC++)
                    lastbyte = char1;
                    //  toss this byte and read the next one
                    char1 = readByteProtected(istream);
                }

            }
	    if (char1 == '>') {
		break;
	    } else {
		msg.setElement(i, char1 & 0xFF);
	    }
        }
    }

    static Logger log = LoggerFactory.getLogger(SerialDCCppPacketizer.class.getName());
}

/* @(#)SerialDCCppPacketizer.java */

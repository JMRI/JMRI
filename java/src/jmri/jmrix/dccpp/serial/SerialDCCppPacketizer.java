/**
 * SerialDCCppPacketizer.java
 */
package jmri.jmrix.dccpp.serial;

import jmri.jmrix.dccpp.DCCppPacketizer;
import jmri.jmrix.dccpp.DCCppReply;
import jmri.jmrix.dccpp.DCCppReplyParser;
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
        return len;
    }

//    /**
//     * Get characters from the input source, and file a message.
//     * <P>
//     * Returns only when the message is complete.
//     * <P>
//     * Only used in the Receive thread.
//     *
//     * @param msg     message to fill
//     * @param istream character source.
//     * @throws java.io.IOException when presented by the input source.
//     */
//    @Override
//    protected void loadChars(jmri.jmrix.AbstractMRReply msg, java.io.DataInputStream istream) throws java.io.IOException {
//        int i;
//        String m;
//        DCCppReply dm;
//        if (log.isDebugEnabled()) {
//            log.debug("loading characters from port");
//        }
//        
//        if (!(msg instanceof DCCppReply)) {
//            log.error("SerialDCCppPacketizer.loadChars called on non-DCCppReply msg!");
//            return;
//        }
//        
//        dm = (DCCppReply)msg;
//        
//        byte char1 = readByteProtected(istream);
//        m = "";
//        while (char1 != '<') {
//            // Spin waiting for '<'
//            char1 = readByteProtected(istream);
//        }
//        log.debug("Serial: Message started...");
//        // Pick up the rest of the command
//        for (i = 0; i < msg.maxSize(); i++) {
//            char1 = readByteProtected(istream);
//	    if (char1 == '>') {
//                log.debug("Received: {}", m);
//                // NOTE: Cast is OK because we checked runtime type of msg above.
//                ((DCCppReply)msg).parseReply(m);
//		break;
//	    } else {
//                m += Character.toString((char)char1);
//                //char1 = readByteProtected(istream);
//                log.debug("msg char[{}]: {} ({})", i, char1, Character.toString((char)char1));
//		//msg.setElement(i, char1 & 0xFF);
//	    }
//        }
//    }

    static Logger log = LoggerFactory.getLogger(SerialDCCppPacketizer.class.getName());
}

/* @(#)SerialDCCppPacketizer.java */

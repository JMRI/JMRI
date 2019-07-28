/**
 * ZTC640XNetPacketizer.java
 */
package jmri.jmrix.lenz.ztc640;

import jmri.jmrix.lenz.XNetPacketizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an extention of the XNetPacketizer to handle the device specific
 * requirements of the ZTC640.
 * <p>
 * In particular, ZTC640XNetPacketizer adds functions to add and remove the 0xFF
 * bytes that appear prior to some messages.
 *
 * @author Paul Bender Copyright (C) 2006
 *
 */
public class ZTC640XNetPacketizer extends XNetPacketizer {

    public ZTC640XNetPacketizer(jmri.jmrix.lenz.LenzCommandStation pCommandStation) {
        super(pCommandStation);
        log.debug("Loading ZTC640 Extention to XNetPacketizer");
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
        if (log.isDebugEnabled()) {
            log.debug("loading characters from port");
        }
        for (i = 0; i < msg.maxSize(); i++) {
            byte char1 = readByteProtected(istream);
            // This is a test for the ZTC640 device
            while ((i == 0) && ((char1 & 0xF0) == 0xF0)) {
                if ((char1 & 0xFF) != 0xF0 && (char1 & 0xFF) != 0xF2) {
                    if (log.isDebugEnabled()) {
                        log.debug("Filtering 0x" + Integer.toHexString(char1 & 0xFF) + " from stream");
                    }
                    //  toss this byte and read the next one
                    char1 = readByteProtected(istream);
                }
            }
            msg.setElement(i, char1 & 0xFF);
            if (endOfMessage(msg)) {
                break;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Accepted Message: " + msg.toString());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ZTC640XNetPacketizer.class);
}



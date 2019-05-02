/**
 * ZTC611XNetPacketizer.java
 */
package jmri.jmrix.ztc.ztc611;

import jmri.jmrix.lenz.XNetPacketizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an extention of the XNetPacketizer to handle the device specific
 * requirements of the ZTC611.
 * <p>
 * In particular, ZTC611XNetPacketizer adds functions to add and remove the 0xFF
 * bytes that appear prior to some messages.
 *
 * @author Paul Bender Copyright (C) 2006
 */
public class ZTC611XNetPacketizer extends XNetPacketizer {

    public ZTC611XNetPacketizer(jmri.jmrix.lenz.LenzCommandStation pCommandStation) {
        super(pCommandStation);
        log.debug("Loading ZTC611 Extention to XNetPacketizer");
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
        log.debug("loading characters from port");
        for (i = 0; i < msg.maxSize(); i++) {
            byte char1 = readByteProtected(istream);
            // This is a test for the ZTC611 device
            while ((i == 0) && ((char1 & 0xF0) == 0xF0)) {
                if ((char1 & 0xFF) != 0xF0 && (char1 & 0xFF) != 0xF2) {
                    if (log.isDebugEnabled()) {
                        log.debug("Filtering 0x{} from stream", Integer.toHexString(char1 & 0xFF));
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
            log.debug("Accepted Message: {}", msg.toString());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ZTC611XNetPacketizer.class);

}

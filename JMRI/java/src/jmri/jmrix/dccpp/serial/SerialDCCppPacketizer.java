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
 * In particular, SerialDCCppPacketizer adds functions to add and remove the
 * {@literal "<" and ">"} bytes that appear around any message read in.
 *
 * Note that the bracket-adding could be pushed up to DCCppPacketizer, as it is
 * a protocol thing, not an interface implementation thing. We'll come back to
 * that later.
 *
 * @author Paul Bender Copyright (C) 2005
 * @author Mark Underwood Copyright (C) 2015
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
    @Override
    protected int lengthOfByteStream(jmri.jmrix.AbstractMRMessage m) {
        int len = m.getNumDataElements() + 2;
        return len;
    }


    private final static Logger log = LoggerFactory.getLogger(SerialDCCppPacketizer.class);
}



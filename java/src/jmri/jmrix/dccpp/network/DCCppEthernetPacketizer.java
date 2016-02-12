/**
 * DCCppEthernetPacketizer.java
 */
package jmri.jmrix.dccpp.network;
    
import jmri.jmrix.dccpp.serial.SerialDCCppPacketizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an extension of the DCCppPacketizer to handle the device specific
 * requirements of the DCC++ Ethernet.
 * <P>
 * In particular, DCCppEthernetPacketizer counts the number of commands
 * received.
 *
 * Based on LIUSBEthernetXnetPacketizer
 * 
 * @author	Paul Bender, Copyright (C) 2011
 * @author      Mark Underwood, Copyright (C) 2015
 * @version $Revision$
 *
 */
public class DCCppEthernetPacketizer extends jmri.jmrix.dccpp.serial.SerialDCCppPacketizer {

    public DCCppEthernetPacketizer(jmri.jmrix.dccpp.DCCppCommandStation pCommandStation) {
        super(pCommandStation);
        if (log.isDebugEnabled()) {
            log.debug("Loading DCC++ Ethernet Extension to DCCppPacketizer");
        }
    }


    private final static Logger log = LoggerFactory.getLogger(DCCppEthernetPacketizer.class.getName());
}

/* @(#)DCCppEthernetPacketizer.java */

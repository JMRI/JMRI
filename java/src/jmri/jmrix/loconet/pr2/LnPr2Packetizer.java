// LnPr2Packetizer.java
package jmri.jmrix.loconet.pr2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Special LnPr2Packetizer implementation for PR2.
 *
 * Differs only in handling PR2's non-echo
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 * @version $Revision$
 *
 */
public class LnPr2Packetizer extends jmri.jmrix.loconet.LnPacketizer {

    final static boolean fulldebug = false;

    boolean debug = false;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "Only used during system initialization")
    public LnPr2Packetizer() {
        super();
        self = this;
        echo = true;
        debug = log.isDebugEnabled();
    }

    private final static Logger log = LoggerFactory.getLogger(LnPr2Packetizer.class.getName());
}

/* @(#)LnPr2Packetizer.java */

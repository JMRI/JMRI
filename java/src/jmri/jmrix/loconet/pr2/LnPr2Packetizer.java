package jmri.jmrix.loconet.pr2;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Special LnPr2Packetizer implementation for PR2.
 *
 * Differs only in handling PR2's non-echo
 *
 * @author Bob Jacobsen Copyright (C) 2006
 *
 */
public class LnPr2Packetizer extends jmri.jmrix.loconet.LnPacketizer {

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", // NOI18N
            justification = "Only used during system initialization") // NOI18N
    public LnPr2Packetizer() {
        super();
        self = this;
        echo = true;
    }

//    private final static Logger log = LoggerFactory.getLogger(LnPr2Packetizer.class);
}

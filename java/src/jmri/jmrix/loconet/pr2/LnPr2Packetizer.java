package jmri.jmrix.loconet.pr2;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.loconet.LnPacketizer;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Special LnPr2Packetizer implementation for PR2.
 * Differs only in handling PR2's non-echo.
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class LnPr2Packetizer extends LnPacketizer {

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", // NOI18N
            justification = "Only used during system initialization") // NOI18N
    public LnPr2Packetizer() {
        super(new LocoNetSystemConnectionMemo());
        echo = true;
    }
    
    // 
    /**
     * Create a Packetizer against an existing LocoNetSystemConnectionMemo.
     * <p>
     * This allows for re-configuring an existing LocoNetSystemConnectionMemo, 
     * which was created during PR3Adapter initialization, for use in the PR3's 
     * "PR2 Mode" (i.e. "Standalone Programmer Mode".)
     *
     * @param memo pre-existing LocoNetSystemConnectionMemo
     */    
    public LnPr2Packetizer(LocoNetSystemConnectionMemo memo) {
        super(memo);
        echo = true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnPr2Packetizer.class);

}

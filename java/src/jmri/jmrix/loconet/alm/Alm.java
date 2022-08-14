package jmri.jmrix.loconet.alm;

import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ALM Message Helper.
 * 
 * @author Bob Milhaupt    Copyright (C) 2022
 */
public class Alm {
    private Alm () {
        throw new IllegalStateException("Utility class"); // NOI18N
    }

    private static final LocoNetMessage almcapq = new LocoNetMessage(new int[] {
        LnConstants.OPC_IMM_PACKET_2, 16, 1, 0,0,0,0,0,
        0,0,0,0,0,0,0,0});
    
//    private static final LocoNetMessage almcapr = new LocoNetMessage(new int[] {
//        LnConstants.OPC_IMM_PACKET_2, 16, 1, 0,0,0,0,0,
//        0,0,0,0,0,0,0,0});
    
    private static final int[] capqmask = {255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255,255, 255, 255, 255, 0};

//    private static final LocoNetMessage cmdStnRoutesCapabilities = new LocoNetMessage(new int[] {
//                        0xE6, 0x10, 0x01, 0x00, 0x00, 0x02, 0x03, 0x02, 0x10,
//                        0x7F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x64}
//                    );
    
    private static final LocoNetMessage drcDs74 = new LocoNetMessage(new int[] {
        0xE6, 0x10, 0x02, 0x00, 0x10, 0x00, 0x00, 0x02,
        0x08, 0x74, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    });
    
    private static final int[] ds74CapabilitiesMasks = {
        0xff, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f,
        0x7f, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

    private static final LocoNetMessage drcDs78v = new LocoNetMessage(new int[] {
        0xE6, 0x10, 0x02, 0x00, 0x20, 0x00, 0x00, 0x02,
        0x08, 0x7C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    });
    
    private static final LocoNetMessage almchga = new LocoNetMessage (new int[] {
        0xEE, 0x10, 2, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
    private static final int[] almchgam = {
        255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0};
    
    private static final LocoNetMessage almgetr = new LocoNetMessage (new int[] {
        0xEE, 0x10, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
    private static final int[] almgetrm = {
        255, 255, 255, 255, 0, 0x7E, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0};

    public static boolean isDs74CapsRpt(LocoNetMessage l) {
        return l.equals(drcDs74, ds74CapabilitiesMasks);
    }

    public static boolean isDs78vCapsRpt(LocoNetMessage l) {
        return l.equals(drcDs78v, ds74CapabilitiesMasks);
    }

    public static boolean isDevBAW(LocoNetMessage l) {
        return (getAlmMsgType(l) == AlmMsgTypes.ALM_BAW);
    }
    
    public static boolean isDs7xRQ(LocoNetMessage l) {
        return getAlmMsgType(l) == AlmMsgTypes.ALM_RDQ;
    }

    static AlmMsgTypes getAlmMsgType(LocoNetMessage m) {
        // Verify message basics
        if (((m.getOpCode() != LnConstants.OPC_IMM_PACKET_2)
                && (m.getOpCode() != LnConstants.OPC_ALM_READ))
                || (m.getElement(1) != 0x10) || (m.getNumDataElements()!=16)) {
            return AlmMsgTypes.NOT_ALM_MSG;
        }

        // check routes capabilities query
        if (m.equals(almcapq)) {
            return AlmMsgTypes.ALM_ROUTCAPQ;
        }
        if (m.equals(almcapq, capqmask)) {
            return AlmMsgTypes.ALM_ROUTCAPREP;
        }
        if (m.equals(almchga, almchgam)) {
            return AlmMsgTypes.ALM_BAW;
        }
        if (m.equals(almgetr, almgetrm)) {
            return AlmMsgTypes.ALM_RDQ;
        }
        return AlmMsgTypes.NOT_ALM_MSG;
    }
//    private final static Logger log = LoggerFactory.getLogger(Alm.class);

}

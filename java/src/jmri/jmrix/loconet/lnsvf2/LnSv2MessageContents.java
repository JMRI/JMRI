/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.loconet.lnsvf2;

import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.text.*;

/**
 *
 * @author given
 */
public class LnSv2MessageContents {
    private int src;
    private int sv_cmd;
    private int sv_type;
    private int dst_l;
    private int dst_h;
    private int dst;
    private int sv_adrl;
    private int sv_adrh;
    private int sv_adr;
    private int d1;
    private int d2;
    private int d3;
    private int d4;

    // LocoNet "SV 2 format" helper definitions: indexes into the LocoNet message
    public final static int SV2_SRC_ELEMENT_INDEX = 2;
    public final static int SV2_SV_CMD_ELEMENT_INDEX = 3;
    public final static int SV2_SV_TYPE_ELEMENT_INDEX = 4;
    public final static int SV2_SVX1_ELEMENT_INDEX = 5;
    public final static int SV2_SV_DST_L_ELEMENT_INDEX = 6;
    public final static int SV2_SV_DST_H_ELEMENT_INDEX = 7;
    public final static int SV2_SV_ADRL_ELEMENT_INDEX = 8;
    public final static int SV2_SV_ADRH_ELEMENT_INDEX = 9;
    public final static int SV2_SVX2_ELEMENT_INDEX = 10;
    public final static int SV2_SVD1_ELEMENT_INDEX = 11;
    public final static int SV2_SVD2_ELEMENT_INDEX = 12;
    public final static int SV2_SVD3_ELEMENT_INDEX = 13;
    public final static int SV2_SVD4_ELEMENT_INDEX = 14;
    
//  helpers for decoding SV format 2 messages (versus other OCP_PEER_XFER messages with length 0x10)
    public final static int SV2_SVX1_ELEMENT_VALIDITY_CHECK_MASK = 0x70;
    public final static int SV2_SVX1_ELEMENT_VALIDITY_CHECK_VALUE = 0x10;
    public final static int SV2_SV_DST_L_DSTLX7_CHECK_MASK = 0x01;
    public final static int SV2_SV_DST_H_DSTHX7_CHECK_MASK = 0x02;
    public final static int SV2_SV_ADRL_SVADRL7_CHECK_MASK = 0x04;
    public final static int SV2_SV_ADRH_SVADRH7_CHECK_MASK = 0x08;
    public final static int SV2_SVX2_ELEMENT_VALIDITY_CHECK_MASK = 0x70;
    public final static int SV2_SVX2_ELEMENT_VALIDITY_CHECK_VALUE = 0x10;
    public final static int SV2_SV_D1_D1X7_CHECK_MASK = 0x01;
    public final static int SV2_SV_D2_D2X7_CHECK_MASK = 0x02;
    public final static int SV2_SV_D3_D3X7_CHECK_MASK = 0x04;
    public final static int SV2_SV_D4_D4X7_CHECK_MASK = 0x08;
    public final static int SV2_SV_TYPE_ELEMENT_VALIDITY_CHECK_MASK = 0x7F;
    public final static int SV2_SV_TYPE_ELEMENT_VALIDITY_CHECK_VALUE = 0x02;

// helpers for decoding SV_CMD
    public final static int SV_CMD_WRITE_ONE = 0x01;
    public final static int SV_CMD_WRITE_ONE_REPLY = 0x41; // reply to SV_CMD_WRITE_ONE
    
    public final static int SV_CMD_QUERY_ONE = 0x02;
    public final static int SV_CMD_REPORT_ONE = 0x42;   // reply to SV_CMD_QUERY_ONE
    
    public final static int SV_CMD_WRITE_ONE_MASKED = 0x03;
    public final static int SV_CMD_WRITE_ONE_MASKED_REPLY = 0x43;   // reply to SV_CMD_WRITE_ONE_MASKED
    
    public final static int SV_CMD_WRITE_FOUR = 0x05;
    public final static int SV_CMD_WRITE_FOUR_REPLY = 0x45;   // reply to SV_CMD_WRITE_FOUR
    
    public final static int SV_CMD_QUERY_FOUR = 0x06;
    public final static int SV_CMD_REPORT_FOUR = 0x46;   // reply to SV_CMD_QUERY_FOUR
    
    public final static int SV_CMD_DISCOVER_DEVICES_QUERY = 0x07;
    public final static int SV_CMD_DISCOVER_DEVICE_REPORT = 0x47;   // reply to SV_CMD_DISCOVER_DEVICES_QUERY
    
    public final static int SV_CMD_IDENTIFY_DEVICE_BY_DEVICE_ADDRESS = 0x08;
    public final static int SV_CMD_IDENTIFY_DEVICE_BY_DEVICE_ADDRESS_REPLY = 0x48;   // reply to SV_CMD_IDENTIFY_DEVICE_BY_DEVICE_ADDRESS
    
    public final static int SV_CMD_CHANGE_ADDRESS_REQUEST = 0x09;
    public final static int SV_CMD_CHANGE_ADDRESS_REPLY = 0x49;   // reply to SV_CMD_CHANGE_ADDRESS_REQUEST
    
    public final static int SV_CMD_RECONFIGURE_REQUEST = 0x0F;
    public final static int SV_CMD_RECONFIGURE_REPLY = 0x4F;   // reply to SV_CMD_RECONFIGURE_REQUEST

// LocoNet "SV 2 format" helper definitions: SV_CMD "reply" bit
    public final static int SV2_SV_CMD_REPLY_BIT_NUBMER = 0x6;
    public final static int SV2_SV_CMD_REPLY_BIT_MASK = (2^SV2_SV_CMD_REPLY_BIT_NUBMER);

    public LnSv2MessageContents(LocoNetMessage m)
            throws java.lang.IllegalArgumentException {
        log.debug("interpreting a LocoNet message - may be an SV2 message");  // NOI18N
        if (!isSupportedSv2Message(m)) {
            log.debug("interpreting a LocoNet message is NOT an SV2 message");   // NOI18N
            throw new java.lang.IllegalArgumentException("LocoNet message is not an SV2 message"); // NOI18N
        }
        src = m.getElement(SV2_SRC_ELEMENT_INDEX);
        int svx1 = m.getElement(SV2_SVX1_ELEMENT_INDEX);
        int svx2 = m.getElement(SV2_SVX2_ELEMENT_INDEX);
        sv_cmd = m.getElement(SV2_SV_CMD_ELEMENT_INDEX);
        sv_type = m.getElement(SV2_SV_TYPE_ELEMENT_INDEX);
        dst_l = m.getElement(SV2_SV_DST_L_ELEMENT_INDEX)
                + (((svx1 & SV2_SV_DST_L_DSTLX7_CHECK_MASK) == SV2_SV_DST_L_DSTLX7_CHECK_MASK)
                ? 0x80 : 0);
        dst_h = m.getElement(SV2_SV_DST_H_ELEMENT_INDEX)
                + (((svx1 & SV2_SV_DST_H_DSTHX7_CHECK_MASK) == SV2_SV_DST_H_DSTHX7_CHECK_MASK)
                ? 0x80 : 0);
        dst = dst_l + (256 * dst_h);

        sv_adrl = m.getElement(SV2_SV_ADRL_ELEMENT_INDEX)
                + (((svx1 & SV2_SV_ADRL_SVADRL7_CHECK_MASK) == SV2_SV_ADRL_SVADRL7_CHECK_MASK)
                ? 0x80 : 0);
        sv_adrh = m.getElement(SV2_SV_ADRH_ELEMENT_INDEX)
                + (((svx1 & SV2_SV_ADRH_SVADRH7_CHECK_MASK) == SV2_SV_ADRH_SVADRH7_CHECK_MASK)
                ? 0x80 : 0);
        sv_adr = sv_adrl + (256 * sv_adrh);

        d1 = m.getElement(SV2_SVD1_ELEMENT_INDEX)
                + (((svx2 & SV2_SV_D1_D1X7_CHECK_MASK) == SV2_SV_D1_D1X7_CHECK_MASK)
                ? 0x80 : 0);

        d2 = m.getElement(SV2_SVD2_ELEMENT_INDEX)
                + (((svx2 & SV2_SV_D2_D2X7_CHECK_MASK) == SV2_SV_D2_D2X7_CHECK_MASK)
                ? 0x80 : 0);

        d3 = m.getElement(SV2_SVD3_ELEMENT_INDEX)
                + (((svx2 & SV2_SV_D3_D3X7_CHECK_MASK) == SV2_SV_D3_D3X7_CHECK_MASK)
                ? 0x80 : 0);

        d4 = m.getElement(SV2_SVD4_ELEMENT_INDEX)
                + (((svx2 & SV2_SV_D4_D4X7_CHECK_MASK) == SV2_SV_D4_D4X7_CHECK_MASK)
                ? 0x80 : 0);
        return;
    }

    public static boolean isSupportedSv2Message(LocoNetMessage m) {
        // must be OPC_PEER_XFER opcode
        if (m.getElement(0) != LnConstants.OPC_PEER_XFER) { 
            log.debug ("cannot be SV2 message because not OPC_PEER_XFER");  // NOI18N
            return false;
        }
        
        // length of OPC_PEER_XFER must be 0x10
        if (m.getElement(1) != 0x10) {
            log.debug ("cannot be SV2 message because not length 0x10");  // NOI18N
            return false;
            }
        
        // <SV_TYPE> must be correct
        if ((m.getElement(SV2_SV_TYPE_ELEMENT_INDEX) 
                & SV2_SV_TYPE_ELEMENT_VALIDITY_CHECK_MASK)
                != SV2_SV_TYPE_ELEMENT_VALIDITY_CHECK_VALUE) {
            log.debug ("cannot be SV2 message because type byte not correct");  // NOI18N
            return false;
        }
        
        // "extended command" identifier must be correct.  Check part of the 
        // "extended command" identifier
        if ((m.getElement(SV2_SVX1_ELEMENT_INDEX) 
                & SV2_SVX1_ELEMENT_VALIDITY_CHECK_MASK) 
                != SV2_SVX1_ELEMENT_VALIDITY_CHECK_VALUE) {
            log.debug ("cannot be SV2 message because SVX1 upper nibble wrong");  // NOI18N
            return false;
        }
        // "extended command" identifier must be correct.  Check the rest
        // of the "extended command" identifier
        if ((m.getElement(SV2_SVX2_ELEMENT_INDEX) 
                & SV2_SVX2_ELEMENT_VALIDITY_CHECK_MASK) 
                != SV2_SVX2_ELEMENT_VALIDITY_CHECK_VALUE) {
            log.debug ("cannot be SV2 message because SVX2 upper nibble wrong");  // NOI18N
            return false;
        }
        
        // check the <SV_CMD> value
        if (isSupportedSv2Command(m.getElement(SV2_SV_CMD_ELEMENT_INDEX))) {
            log.debug("LocoNet message is a supported SV Format 2 message");
            return true;
        }
        log.debug("LocoNet message is not a supported SV Format 2 message");  // NOI18N
        return false;
    }
    
    public static boolean isSupportedSv2Message(LocoNetMessage m, Sv2MessageType svMsgType) {
        // must be OPC_PEER_XFER opcode
        if (m.getElement(0) != LnConstants.OPC_PEER_XFER) { 
            log.debug ("cannot be SV2 message because not OPC_PEER_XFER");  // NOI18N
            return false;
        }
        
        // length of OPC_PEER_XFER must be 0x10
        if (m.getElement(1) != 0x10) {
            log.debug ("cannot be SV2 message because not length 0x10");  // NOI18N
            return false;
            }
        
        // <SV_TYPE> must be correct
        if ((m.getElement(SV2_SV_TYPE_ELEMENT_INDEX) 
                & SV2_SV_TYPE_ELEMENT_VALIDITY_CHECK_MASK)
                != SV2_SV_TYPE_ELEMENT_VALIDITY_CHECK_VALUE) {
            log.debug ("cannot be SV2 message because type byte not correct");  // NOI18N
            return false;
        }
        
        // "extended command" identifier must be correct.  Check part of the 
        // "extended command" identifier
        if ((m.getElement(SV2_SVX1_ELEMENT_INDEX) 
                & SV2_SVX1_ELEMENT_VALIDITY_CHECK_MASK) 
                != SV2_SVX1_ELEMENT_VALIDITY_CHECK_VALUE) {
            log.debug ("cannot be SV2 message because SVX1 upper nibble wrong");  // NOI18N
            return false;
        }
        // "extended command" identifier must be correct.  Check the rest
        // of the "extended command" identifier
        if ((m.getElement(SV2_SVX2_ELEMENT_INDEX) 
                & SV2_SVX2_ELEMENT_VALIDITY_CHECK_MASK) 
                != SV2_SVX2_ELEMENT_VALIDITY_CHECK_VALUE) {
            log.debug ("cannot be SV2 message because SVX2 upper nibble wrong");  // NOI18N
            return false;
        }
        
        // check the <SV_CMD> value
        if (isSupportedSv2Command(m.getElement(SV2_SV_CMD_ELEMENT_INDEX))) {
            log.debug("LocoNet message is a supported SV Format 2 message");  // NOI18N
            if (extractMessageType(m).equals(svMsgType)) {
                log.debug("LocoNet message is the specified SV Format 2 message");  // NOI18N
                return true;
            }
        }
        log.debug("LocoNet message is not a supported SV Format 2 message");  // NOI18N
        return false;
    }
    
    public static Sv2MessageType extractMessageType(LocoNetMessage m) {
        if (isSupportedSv2Message(m)) {
            int msgCmd = m.getElement(SV2_SV_CMD_ELEMENT_INDEX);
            for (Sv2MessageType s: Sv2MessageType.values()) {
                if (s.getCmd() == msgCmd) {
                    log.debug("LocoNet message has SV2 message command " + msgCmd);  // NOI18N
                    return s;
                }
            }
        }
        return null;
    }
    
    public String interpretSv2Message() {
        Locale l = Locale.getDefault();
        return interpretSv2Message(l);
    }
    
    public String interpretSv2Message(Locale locale) {
        String returnString;
        log.debug("interpreting an SV2 message - cmd is "+sv_cmd);  // NOI18N
        
        switch (sv_cmd) {
            case (SV_CMD_WRITE_ONE):
                returnString = Bundle.getMessage(locale, "SV2_WRITE_ONE_INTERPRETED", 
                        src,
                        dst,
                        sv_adr,
                        d1);
                break;
            case (SV_CMD_WRITE_ONE_REPLY):    
                returnString = Bundle.getMessage(locale, "SV2_WRITE_ONE_REPLY_INTERPRETED", 
                        src,
                        dst,
                        sv_adr,
                        d1);
                break;
            case (SV_CMD_QUERY_ONE):
                returnString = Bundle.getMessage(locale, "SV2_READ_ONE_REQUEST_INTERPRETED", 
                        src,
                        dst,
                        sv_adr);
                break;
            case (SV_CMD_REPORT_ONE):    
                returnString = Bundle.getMessage(locale, "SV2_READ_ONE_REPORT_INTERPRETED", 
                        src,
                        dst,
                        sv_adr,
                        d1);
                break;
            case (SV_CMD_WRITE_ONE_MASKED):
                returnString = Bundle.getMessage(locale, "SV2_WRITE_ONE_MASKED_INTERPRETED", 
                        src,
                        dst,
                        sv_adr,
                        d1,
                        d2);
                break;
            case (SV_CMD_WRITE_ONE_MASKED_REPLY):    
                returnString = Bundle.getMessage(locale, "SV2_WRITE_ONE_MASKED_REPLY_INTERPRETED", 
                        src,
                        dst,
                        sv_adr,
                        d1,
                        d2);
                break;
            case (SV_CMD_WRITE_FOUR):
                /* Note: This code does not track total available SVs.  Total 
                        available SVs can vary by SV device type.  So the simple 
                        expedient used here is "last SV number is equal to first 
                        SV number plus 3".
                        */
                returnString = Bundle.getMessage(locale, "SV2_WRITE_FOUR_INTERPRETED", 
                        src,
                        dst,
                        sv_adr,
                        sv_adr+3,
                        d1,
                        d2,
                        d3,
                        d4);
                break;
            case (SV_CMD_WRITE_FOUR_REPLY):    
                /* Note: This code does not track total available SVs.  Total 
                        available SVs can vary by SV device type.  So the simple 
                        expedient used here is "last SV number is equal to first 
                        SV number plus 3".
                        */
                returnString = Bundle.getMessage(locale, "SV2_WRITE_FOUR_REPLY_INTERPRETED", 
                        src,
                        dst,
                        sv_adr,
                        sv_adr+3,
                        d1,
                        d2,
                        d3,
                        d4);
                break;
            case (SV_CMD_QUERY_FOUR):
                /* Note: This code does not track total available SVs.  Total 
                        available SVs can vary by SV device type.  So the simple 
                        expedient used here is "last SV number is equal to first 
                        SV number plus 3".
                        */
                returnString = Bundle.getMessage(locale, "SV2_READ_FOUR_REQUEST_INTERPRETED", 
                        src,
                        dst,
                        sv_adr,
                        sv_adr+3);
                break;
            case (SV_CMD_REPORT_FOUR):    
                /* Note: This code does not track total available SVs.  Total 
                        available SVs can vary by SV device type.  So the simple 
                        expedient used here is "last SV number is equal to first 
                        SV number plus 3".
                        */
                returnString = Bundle.getMessage(locale, "SV2_READ_FOUR_REPORT_INTERPRETED", 
                        src,
                        dst,
                        sv_adr,
                        sv_adr+3,
                        d1,
                        d2,
                        d3,
                        d4);
                break;
            case (SV_CMD_DISCOVER_DEVICES_QUERY):
                returnString = Bundle.getMessage(locale, "SV2_DISCOVER_DEVICES_INTERPRETED", 
                        src);
                break;
            case (SV_CMD_DISCOVER_DEVICE_REPORT):
                returnString = Bundle.getMessage(locale, "SV2_DEVICE_TYPE_REPORT_INTEPRETED",
                        src,
                        dst,
                        sv_adrl,
                        sv_adrh,
                        d1 + (256 * d2),
                        d3 + (256 * d4));
                break;
            case (SV_CMD_IDENTIFY_DEVICE_BY_DEVICE_ADDRESS):
                returnString = Bundle.getMessage(locale, "SV2_IDENTIFY_DEVICE_REQUEST_INTEPRETED",
                        src,
                        dst);
                break;
            case (SV_CMD_IDENTIFY_DEVICE_BY_DEVICE_ADDRESS_REPLY):    
                returnString = Bundle.getMessage(locale, "SV2_DEVICE_IDENTITY_REPORT_INTEPRETED",
                        src,
                        dst,                // SV device address
                        sv_adrl,            // manufacturer id
                        sv_adrh,            // device id
                        d1 + (256 * d2),    // product id
                        d3 + (256 * d4));   // serial number
                break;
            case (SV_CMD_CHANGE_ADDRESS_REQUEST):
                returnString = Bundle.getMessage(locale, "SV2_CHANGE_ADDRESS_REQUEST_INTEPRETED",
                        src,
                        dst, // <new> SV device address
                        sv_adrl,            // manufacturer id
                        sv_adrh,            // device id
                        d1 + (256 * d2),    // product id
                        d3 + (256 * d4));   // serial number
                break;
            case (SV_CMD_CHANGE_ADDRESS_REPLY):
                /*
                Using only a single SV2 Programming Format message, it is impossible 
                to correctly distinguish between a change address reply which indicates a
                need for a "Reconfigure" message from a change address reply which indicates 
                that a reconfigure is not required.  This code does a "best guess" by looking
                at the other data fields.  */
                if ((sv_adrl == 0) && 
                        (sv_adrh == 0) && 
                        (d1 == 0) &&
                        (d2 == 0) &&
                        (d3 == 0) &&
                        (d4 == 0)) {
                    // this is probably a change address reply where a reconfigure is required
                returnString = Bundle.getMessage(locale, "SV2_CHANGE_ADDRESS_REPLY_NEEDS_RECONFIGURE_INTEPRETED",
                        src,
                        dst // old SV device address
                        );
                break;
                } else 
                returnString = Bundle.getMessage(locale, "SV2_CHANGE_ADDRESS_REPLY_INTEPRETED",
                        src,
                        dst, // new SV device address
                        sv_adrl,            // manufacturer id
                        sv_adrh,            // device id
                        d1 + (256 * d2),    // product id
                        d3 + (256 * d4));   // serial number
                break;
            case (SV_CMD_RECONFIGURE_REQUEST):
                returnString = Bundle.getMessage(locale, "SV2_RECONFIGURE_REQUEST_INTEPRETED", 
                        src,
                        dst);
                break;
            case (SV_CMD_RECONFIGURE_REPLY):
                returnString = Bundle.getMessage(locale, "SV2_DEVICE_RECONFIGURE_REPLY_INTEPRETED",
                        src,
                        dst,                // SV device address
                        sv_adrl,            // manufacturer id
                        sv_adrh,            // device id
                        d1 + (256 * d2),    // product id
                        d3 + (256 * d4));   // serial number
                break;
            default:
                return Bundle.getMessage(locale, "SV2_UNDEFINED_MESSAGE");
        }
        log.debug("interpreted: " + returnString);  // NOI18N
        return returnString+"\n";  // NOI18N
    }

    public static boolean isSupportedSv2Command(int possibleCmd) {
        switch (possibleCmd) {
            case (SV_CMD_WRITE_ONE):
            case (SV_CMD_WRITE_ONE_REPLY):    
            case (SV_CMD_QUERY_ONE):
            case (SV_CMD_REPORT_ONE):    
            case (SV_CMD_WRITE_ONE_MASKED):
            case (SV_CMD_WRITE_ONE_MASKED_REPLY):    
            case (SV_CMD_WRITE_FOUR):
            case (SV_CMD_WRITE_FOUR_REPLY):    
            case (SV_CMD_QUERY_FOUR):
            case (SV_CMD_REPORT_FOUR):    
            case (SV_CMD_DISCOVER_DEVICES_QUERY):
            case (SV_CMD_DISCOVER_DEVICE_REPORT):    
            case (SV_CMD_IDENTIFY_DEVICE_BY_DEVICE_ADDRESS):
            case (SV_CMD_IDENTIFY_DEVICE_BY_DEVICE_ADDRESS_REPLY):    
            case (SV_CMD_CHANGE_ADDRESS_REQUEST):
            case (SV_CMD_CHANGE_ADDRESS_REPLY):    
            case (SV_CMD_RECONFIGURE_REQUEST):
            case (SV_CMD_RECONFIGURE_REPLY):
                return true;
            default:
                return false;
        }
    }    
    
    public boolean isSupportedSv2Command() {
        return isSupportedSv2Command(sv_cmd);
    }
    
    public boolean isSupportedSv2ReadOneReply() {
        return (sv_cmd == SV_CMD_REPORT_ONE);
    }
    public boolean isSupportedSv2ReadFourReply() {
        return (sv_cmd == SV_CMD_REPORT_ONE);
    }
    public boolean isSupportedSv2ReadOneReplyOrSv2ReadFourReply() {
        return ((sv_cmd == SV_CMD_REPORT_ONE)
                ||
                (sv_cmd == SV_CMD_REPORT_FOUR));
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(LnSv2MessageContents.class.getName());
    
    public int getSingleReadReportData() {
        return d1;
    }

    public enum Sv2MessageType {
        SV2_WRITE_ONE (0x01),
        SV2_QUERY_ONE (0x02),
        SV2_WRITE_ONE_MASKED (0x03),
        SV2_WRITE_FOUR (0x05),
        SV2_QUERY_FOUR (0x06),
        SV2_DISCOVER_ALL (0x07),
        SV2_IDENTIFY_DEVICES_BY_TYPE (0x08),
        SV2_CHANGE_DEVICE_ADDRESS (0x09),
        SV2_RECONFIGURE_DEVICE (0x0f),
        SV2_WRITE_ONE_REPLY (0x41),
        SV2_REPORT_ONE (0x42),
        SV2_WRITE_ONE_MASKED_REPLYL (0x43),
        SV2_WRITE_FOUR_REPLY (0x45),
        SV2_REPORT_FOUR (0x46),
        SV2_DISCOVER_DEVICE_REPORT (0x47),
        SV2_DEVICE_TYPE_REPORT (0x48),
        SV2_CHANGE_DEVICE_ADDRESS_REPLY (0x49),
        SV2_RECONFIGURE_DEVICE_REPLY (0x4f);
        
        private int cmd;
        
        Sv2MessageType(int cmd) {
            this.cmd = cmd;
        }
        int getCmd() {return cmd;}
    }
}

package jmri.jmrix.loconet.lnsvf2;

import java.util.Locale;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supporting class for LocoNet SV Programming Format 2 messaging
 * 
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.

 * @author B. Milhaupt Copyright (C) 2015
 */
public class LnSv2MessageContents {
    private int src;
    private int sv_cmd;
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

    // LocoNet "SV 2 format" helper definitions: length byte value for OPC_PEER_XFER message
    public final static int SV2_LENGTH_ELEMENT_VALUE = 0x10;
    
    // LocoNet "SV 2 format" helper definitions: indexes into the LocoNet message
    public final static int SV2_LENGTH_ELEMENT_INDEX = 1;
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
    public final static int SV2_SRC_ELEMENT_MASK = 0x7f;
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

    // LocoNet "SV 2 format" helper definitions for data
    public final static int SV2_SV_DATA_INDEX_EEPROM_SIZE = 1;
    public final static int SV2_SV_DATA_INDEX_SOFTWARE_VERSION = 2;
    public final static int SV2_SV_DATA_INDEX_SERIAL_NUMBER_LOW = 3;
    public final static int SV2_SV_DATA_INDEX_SERIAL_NUMBER_HIGH = 4;
    
            
    /**
     * Create a new LnSV2MessageContents object from a LocoNet message
     * @param m LocoNet message containing an SV Programming Format 2 message
     * @throws IllegalArgumentException if the LocoNet message is not a valid, supported 
     *      SV Programming Format 2 message
     */
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

    /**
     * Check a LocoNet message to determine if it is a valid SV Programming Format 
     *      2 message.
     * @param m  LocoNet message to check
     * @return true if LocoNet message m is a supported SV Programming Format 2
     *      message, else false.
     */
    public static boolean isSupportedSv2Message(LocoNetMessage m) {
        // must be OPC_PEER_XFER opcode
        if (m.getOpCode() != LnConstants.OPC_PEER_XFER) { 
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
    
    /**
     *
     * @param m  LocoNet message to be verified as an SV Programming Format 2 message
     *      with the specified &lt;SV_CMD&gt; value
     * @param svCmd  SV Programming Format 2 command to expect
     * @return true if message is an SV Programming Format 2 message of the specified &lt;SV_CMD&gt;,
     *      else false.
     */
    public static boolean isLnMessageASpecificSv2Command(LocoNetMessage m, Sv2Command svCmd) {
        // must be OPC_PEER_XFER opcode
        if (m.getOpCode() != LnConstants.OPC_PEER_XFER) { 
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
            if (extractMessageType(m).equals(svCmd)) {
                log.debug("LocoNet message is the specified SV Format 2 message");  // NOI18N
                return true;
            }
        }
        log.debug("LocoNet message is not a supported SV Format 2 message");  // NOI18N
        return false;
    }
    
    /**
     * Interprets a LocoNet message to determine its SV Programming Format 2 &lt;SV_CMD&gt;.
     * If the message is not an SV Programming Format 2 message, returns null
     * @param m  LocoNet message containing SV Programming Format 2 message
     * @return Sv2Command found in the SV Programming Format 2 message
     */
    public static Sv2Command extractMessageType(LocoNetMessage m) {
        if (isSupportedSv2Message(m)) {
            int msgCmd = m.getElement(SV2_SV_CMD_ELEMENT_INDEX);
            for (Sv2Command s: Sv2Command.values()) {
                if (s.getCmd() == msgCmd) {
                    log.debug("LocoNet message has SV2 message command " + msgCmd);  // NOI18N
                    return s;
                }
            }
        }
        return null;
    }
    
    /**
     * Interprets the SV Programming Format 2 message into a human-readable string.
     * 
     * @return String containing a human-readable version of the SV Programming 
     *      Format 2 message
     */
    @Override
    public String toString() {
        Locale l = Locale.getDefault();
        return LnSv2MessageContents.this.toString(l);
    }
    
    /**
     * Interprets the SV Programming Format 2 message into a human-readable string.
     * 
     * @param locale  locale to use for the human-readable string
     * @return String containing a human-readable version of the SV Programming 
     *      Format 2 message, in the language specified by the Locale, if the 
     *      properties have been translated to that Locale, else in the deafult 
     *      English language.
     */
    public String toString(Locale locale) {
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

    /**
     *
     * @param possibleCmd  integer to be compared to the command list
     * @return  true if the possibleCmd value is one of the supported SV 
     *      Programming Format 2 commands
     */
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
    
    /**
     *
     * @return true if the SV2 message specifies a valid (known) SV Programming 
     *      Format 2 command.
     */
    public boolean isSupportedSv2Command() {
        return isSupportedSv2Command(sv_cmd);
    }
    
    /**
     *
     * @return true if the SV2 message is a SV2 Read One Reply message
     */
    public boolean isSupportedSv2ReadOneReply() {
        return (sv_cmd == SV_CMD_REPORT_ONE);
    }

    /**
     *
     * @return true of the SV2 message is a SV2 Read Four Reply message
     */
    public boolean isSupportedSv2ReadFourReply() {
        return (sv_cmd == SV_CMD_REPORT_FOUR);
    }

    /**
     *
     * @return true if the SV2 message is a SV2 Read One Reply message or a SV2
     * Read Four Reply message
     */
    public boolean isSupportedSv2ReadOneReplyOrSv2ReadFourReply() {
        return ((sv_cmd == SV_CMD_REPORT_ONE)
                ||
                (sv_cmd == SV_CMD_REPORT_FOUR));
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LnSv2MessageContents.class);
    
    /**
     * Get the data from a SVs Single Read Reply message.  May also be used to
     * return the effective SV value reported in a SV2 Single Write Reply message.
     * @return the {@code <D1>} value from the SV2 message
     */
    public int getSingleReadReportData() {
        return d1;
    }
    /**
     * Create a LocoNet message containing an SV Programming Format 2 message
     * @param source  source device address (7 bit, for &lt;SRC&gt;)
     * @param command  SV Programming Format 2 command number (for &lt;SV_CMD&gt;)
     * @param destination = SV format 2 destination address (for &lt;DST_L&gt; and &lt;DST_H&gt;)
     * @param svNum  SV Programming Format 2 16-bit SV number (for &lt;SVN_L&gt; and &lt;SVN_H&gt;)
     * @param d1  SV Programming Format 2 first data value (for &lt;D1&gt;)
     * @param d2  SV Programming Format 2 second data value (for &lt;D2&gt;)
     * @param d3  SV Programming Format 2 third data value (for &lt;D3&gt;)
     * @param d4  SV Programming Format 2 fourth data value (for &lt;D4&gt;)
     * @return LocoNet message for the requested message
     * @throws IllegalArgumentException of command is not a valid SV Programming Format 2 &lt;SV_CMD&gt; value
     */
    public static LocoNetMessage createSv2Message (int source, int command, 
            int destination, int svNum, int d1, int d2, int d3, int d4) 
        throws java.lang.IllegalArgumentException {
            if ( ! isSupportedSv2Command(command)) {
                throw new java.lang.IllegalArgumentException("Command is not a supported SV2 command"); // NOI18N
            }
        LocoNetMessage m = new LocoNetMessage(SV2_LENGTH_ELEMENT_VALUE);
        m.setOpCode(LnConstants.OPC_PEER_XFER);
        m.setElement(SV2_LENGTH_ELEMENT_INDEX, SV2_LENGTH_ELEMENT_VALUE);
        m.setElement(SV2_SRC_ELEMENT_INDEX, (source & SV2_SRC_ELEMENT_MASK));
        m.setElement(SV2_SV_CMD_ELEMENT_INDEX, command);
        m.setElement(SV2_SV_TYPE_ELEMENT_INDEX, SV2_SV_TYPE_ELEMENT_VALIDITY_CHECK_VALUE);
        
        int svx1 = SV2_SVX1_ELEMENT_VALIDITY_CHECK_VALUE;
        svx1 = svx1 + (((destination & 0x80) == 0x80) ? SV2_SV_DST_L_DSTLX7_CHECK_MASK : 0);
        svx1 = svx1 + (((destination & 0x8000) == 0x8000) ? SV2_SV_DST_H_DSTHX7_CHECK_MASK : 0);
        svx1 = svx1 + (((svNum & 0x80) == 0x80) ? SV2_SV_ADRL_SVADRL7_CHECK_MASK : 0);
        svx1 = svx1 + (((svNum & 0x8000) == 0x8000) ? SV2_SV_ADRH_SVADRH7_CHECK_MASK : 0);
        m.setElement(SV2_SVX1_ELEMENT_INDEX,svx1);
        
        m.setElement(SV2_SV_DST_L_ELEMENT_INDEX, (destination & 0x7f));
        m.setElement(SV2_SV_DST_H_ELEMENT_INDEX, ((destination >> 8) & 0x7f));
        m.setElement(SV2_SV_ADRL_ELEMENT_INDEX, (svNum & 0x7f));
        m.setElement(SV2_SV_ADRH_ELEMENT_INDEX, ((svNum >> 8) & 0x7f));
        
        int svx2 = SV2_SVX2_ELEMENT_VALIDITY_CHECK_VALUE;
        svx2 = svx2 + (((d1 & 0x80) == 0x80) ? SV2_SV_D1_D1X7_CHECK_MASK : 0);
        svx2 = svx2 + (((d2 & 0x80) == 0x80) ? SV2_SV_D2_D2X7_CHECK_MASK : 0);
        svx2 = svx2 + (((d3 & 0x80) == 0x80) ? SV2_SV_D3_D3X7_CHECK_MASK : 0);
        svx2 = svx2 + (((d4 & 0x80) == 0x80) ? SV2_SV_D4_D4X7_CHECK_MASK : 0);
        m.setElement(SV2_SVX2_ELEMENT_INDEX,svx2);
        
        m.setElement(SV2_SVD1_ELEMENT_INDEX, (d1 & 0x7f));
        m.setElement(SV2_SVD2_ELEMENT_INDEX, (d2 & 0x7f));
        m.setElement(SV2_SVD3_ELEMENT_INDEX, (d3 & 0x7f));
        m.setElement(SV2_SVD4_ELEMENT_INDEX, (d4 & 0x7f));
        
        return m;
    }
    
    public int getDestAddr() {
        if (sv_cmd != Sv2Command.SV2_DISCOVER_ALL.cmd) {
            return dst_l + 246*dst_h;
        }
        return -1;
    }
    
    public int getSVNum() {
        if ((sv_cmd != Sv2Command.SV2_DISCOVER_ALL.cmd) && 
                (sv_cmd != Sv2Command.SV2_IDENTIFY_DEVICES_BY_TYPE.cmd) && 
                (sv_cmd != Sv2Command.SV2_CHANGE_DEVICE_ADDRESS.cmd) && 
                (sv_cmd != Sv2Command.SV2_DISCOVER_DEVICE_REPORT.cmd) &&
                (sv_cmd != Sv2Command.SV2_DEVICE_TYPE_REPORT.cmd) && 
                (sv_cmd != Sv2Command.SV2_CHANGE_DEVICE_ADDRESS_REPLY.cmd) &&
                (sv_cmd != Sv2Command.SV2_RECONFIGURE_DEVICE_REPLY.cmd) &&
                (sv_cmd != Sv2Command.SV2_RECONFIGURE_DEVICE.cmd)) {
            return sv_adrl + 256*sv_adrh;
        }
        return -1;
    }
    
    public int getSv2ManufacturerID() {
        if ((sv_cmd == Sv2Command.SV2_DISCOVER_DEVICE_REPORT.cmd) ||
                (sv_cmd == Sv2Command.SV2_DEVICE_TYPE_REPORT.cmd)) {
            return sv_adrl;
        }
        return -1;
    }
    
    public boolean isSvReconfigureReply() {
        if (sv_cmd == Sv2Command.SV2_RECONFIGURE_DEVICE_REPLY.cmd) {
            return true;
        }
        return false;
    }

    public int getSv2DeveloperID() {
        if ((sv_cmd == Sv2Command.SV2_CHANGE_DEVICE_ADDRESS.cmd) ||
                (sv_cmd == Sv2Command.SV2_DISCOVER_DEVICE_REPORT.cmd) || 
                (sv_cmd == Sv2Command.SV2_DEVICE_TYPE_REPORT.cmd)){
            return sv_adrh;
        }
        return -1;
    }

    public int getSv2ProductID() {
        if ((sv_cmd == Sv2Command.SV2_CHANGE_DEVICE_ADDRESS.cmd) ||
                (sv_cmd == Sv2Command.SV2_DISCOVER_DEVICE_REPORT.cmd) || 
                (sv_cmd == Sv2Command.SV2_DEVICE_TYPE_REPORT.cmd) ||
                (sv_cmd == Sv2Command.SV2_RECONFIGURE_DEVICE_REPLY.cmd)){
            return d1 + d2 * 256;
        }
        return -1;
    }

    public int getSv2SerialNum() {
        if ((sv_cmd == Sv2Command.SV2_CHANGE_DEVICE_ADDRESS.cmd) ||
                (sv_cmd == Sv2Command.SV2_DISCOVER_DEVICE_REPORT.cmd) || 
                (sv_cmd == Sv2Command.SV2_DEVICE_TYPE_REPORT.cmd) ||
                (sv_cmd == Sv2Command.SV2_RECONFIGURE_DEVICE_REPLY.cmd)){
            return d3 + d4 * 256;
        }
        return -1;
    }
    
    /** 
     * 
     * @param ida  IDA number for "SRC" field of OPC_PEER_XFER
     * @param currentDest  the destination address of the device
     * @param mfg  the Manufacturer ID
     * @param devel  the developer ID
     * @param prodID  the product ID
     * @param serial  the serial number
     * @return a LocoNet message containing the formatted reply
     */
    public static LocoNetMessage createSv2DeviceDiscoveryReply(int ida, int currentDest, 
            int mfg, int devel, int prodID, int serial) {
    
        return createSv2Message(ida, 
                Sv2Command.SV2_DISCOVER_DEVICE_REPORT.cmd, 
                currentDest, 
                mfg + (256*devel), 
                prodID % 256, 
                prodID / 256, 
                serial % 256, 
                serial/256) ;
    }
    
    /**
     * Creates a LocoNet message for the reply for an SV2 "Change Address" 
     * message where the device requires a reconfigure.
     * 
     * @param ida  IDA value, for the SRC field of the OPC_PEER_XFER
     * @param destAddr  the "old" SV2 destination address
     * @return a LocoNet message
     */
    public static LocoNetMessage createSv2ChangeAddressReply(int ida, int destAddr) {
        return createSv2Message(ida, 
                Sv2Command.SV2_CHANGE_DEVICE_ADDRESS_REPLY.cmd, 
                destAddr, 
                0, 0, 0, 0, 0) ;
    }

    /**
     * Creates a LocoNet message for the reply for an SV2 "Change Address" 
     * message where the device requires a reconfigure.
     * 
     * @param ida  IDA value, for the SRC field of the OPC_PEER_XFER
     * @param newDestAddr  the "new" SV2 destination address
     * @param mfg  manufacturer ID
     * @param developer  device ID
     * @param productId  product ID
     * @param serialNum  serial Number
     * @return a LocoNet message
     */
    public static LocoNetMessage createSv2ChangeAddressReply(int ida, int newDestAddr, 
            int mfg, int developer, int productId, int serialNum) {
        return createSv2Message(ida, 
                Sv2Command.SV2_CHANGE_DEVICE_ADDRESS_REPLY.cmd, 
                newDestAddr, 
                mfg + (256 * developer), productId % 256, productId / 256, 
                serialNum % 256, serialNum / 256);
    }
    
    /**
     * Creates a LocoNet message for the reply for an SV2 "Reconfigure Reply"
     * 
     * @param ida  IDA value, for the SRC field of the OPC_PEER_XFER
     * @param newDestAddr  the "new" SV2 destination address
     * @param mfg  manufacturer ID
     * @param developer  device ID
     * @param productId  product ID
     * @param serialNum  serial Number
     * @return a LocoNet message
     */
    public static LocoNetMessage createSv2ReconfigureReply(int ida, int newDestAddr, 
            int mfg, int developer, int productId, int serialNum) {
        return createSv2Message(ida, 
                Sv2Command.SV2_RECONFIGURE_DEVICE_REPLY.cmd, 
                newDestAddr, 
                mfg + (256 * developer), productId % 256, productId / 256, 
                serialNum % 256, serialNum / 256);
    }
    /**
     * 
     * @param m  the preceeding LocoNet message
     * @param svValues  array containing the SV values; only one value is used 
     *          when m contains a SV_QUERY_ONE, else contains 4 values.
     * @return  LocoNet message containing the reply, or null if preceeding 
     *          message isn't a query.
     */
    public static LocoNetMessage createSvReadReply(LocoNetMessage m, int svValues[]) {
        if (!isSupportedSv2Message(m)) {
            return null;
        }
        if ((m.getElement(3) != Sv2Command.SV2_QUERY_ONE.cmd) && 
                (m.getElement(3) != Sv2Command.SV2_QUERY_FOUR.cmd)) {
            return null;
            
        }
        LocoNetMessage n = m;
        n.setElement(3, n.getElement(3) + 0x40);
        n.setElement(11, svValues[0] & 0x7F);
        if (n.getElement(3) == Sv2Command.SV2_QUERY_ONE.cmd) {
            n.setElement(12, 0);
            n.setElement(13, 0);
            n.setElement(14, 0);
            int a = n.getElement(10);
            a &= 0x70;
            if ((svValues[0] & 0xFF) > 0x7f) {
                a |= 1;
            }
            n.setElement(10, a);
            return n;
        }
        n.setElement(12, svValues[1] & 0x7F);
        n.setElement(13, svValues[2] & 0x7F);
        n.setElement(14, svValues[3] & 0x7F);
        int a = n.getElement(10);
        a &= 0x70;
        a |= ((svValues[1] & 0x80) >> 6);
        a |= ((svValues[2] & 0x80) >> 5);
        a |= ((svValues[3] & 0x80) >> 5);
        n.setElement(10, a);
        return n;
    }

    /**
     * 
     * @param m  the preceeding LocoNet message
     * @param svValue  value of one SV register
     * @return  LocoNet message containing the reply, or null if preceeding 
     *          message isn't a query.
     */
    public static LocoNetMessage createSvReadReply(LocoNetMessage m, int svValue) {
        return createSvReadReply(m, new int[] {svValue});
    }

    /**
     * Get the d1 value
     * @return d1
     */
    public int getSv2D1() {
        return d1;
    }
    
    /**
     * Get the d2 value
     * @return d2
     */
    public int getSv2D2() {
        return d2;
    }
    
    /**
     * Get the d3 value
     * @return d3
     */
    public int getSv2D3() {
        return d3;
    }
    
    /**
     * Get the d4 value
     * @return d4
     */
    public int getSv2D4() {
        return d4;
    }

    public boolean isSvChangeAddressReply() {
        if (sv_cmd == Sv2Command.SV2_CHANGE_DEVICE_ADDRESS_REPLY.cmd) {
            return true;
        }
        return false;
    }
    
    public static LocoNetMessage createSvDiscoverQueryMessage() {
     return createSv2Message(1, 
                Sv2Command.SV2_DISCOVER_ALL.cmd, 
                0, 0, 0, 0, 0, 0);
     }
    
    public static LocoNetMessage createSvReadRequest() {
     return createSv2Message(1, 
                Sv2Command.SV2_DISCOVER_ALL.cmd, 
                0, 0, 0, 0, 0, 0);
     }

    /**
     * Create LocoNet message for another query of an SV of this object
     * 
     * @param deviceAddress  address of the device
     * @param svNum  SV number
     * @return LocoNet message
     */
    public static LocoNetMessage createSvReadRequest(int deviceAddress, int svNum) {
        return createSv2Message(1, Sv2Command.SV2_QUERY_ONE.cmd,
                deviceAddress, svNum, 0, 0, 0, 0);
    }

    public enum Sv2Command {
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
        
        Sv2Command(int cmd) {
            this.cmd = cmd;
        }
        int getCmd() {return cmd;}
        
        public static int getCmd(Sv2Command mt) {
            return mt.getCmd();
        }
    }
}

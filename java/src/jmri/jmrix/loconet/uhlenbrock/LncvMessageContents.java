package jmri.jmrix.loconet.uhlenbrock;

import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Objects;

/**
 * Supporting class for Uhlenbrock LocoNet LNCV Programming Format messaging.
 * Structure adapted from {@link jmri.jmrix.loconet.lnsvf2.LnSv2MessageContents}
 * 
 * Some of the message formats used in this class are Copyright Uhlenbrock
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Uhlenbrock
 * for separate permission.

 * @author Egbert Broerse Copyright (C) 2020
 */
public class LncvMessageContents {
    private int src;
    private int dst_l;
    private int dst_h;
    private int dst;
    private int cmd;
    private int art_l;
    private int art_h;
    private int art;
    private int cvn_l;
    private int cvn_h;
    private int cvn;
    private int mod_l;
    private int mod_h;
    private int mod;
    private int cmd_data;

    // LocoNet "LNCV format" helper definitions: length byte value for LNCV message
    public final static int LNCV_LENGTH_ELEMENT_VALUE = 0x0F;
    public final static int LNCV_LNMODULE_VALUE = 0x05;
    public final static int LNCV_CS_SRC_VALUE = 0x01;
    public final static int LNCV_CSDEST_VALUE = 0x4B49;
    public final static int LNCV_ALL = 0xFFFF; // decimal 65535
    // the valid range for module addresses (CV0) as per the LNCV spec.
    public final static int LNCV_MIN_MODULEADDR = 0;
    public final static int LNCV_MAX_MODULEADDR = 65534;

    // LocoNet "LNCV format" helper definitions: indexes into the LocoNet message
    public final static int LNCV_LENGTH_ELEMENT_INDEX = 1;
    public final static int LNCV_SRC_ELEMENT_INDEX = 2;
    public final static int LNCV_DST_L_ELEMENT_INDEX = 3;
    public final static int LNCV_DST_H_ELEMENT_INDEX = 4;
    public final static int LNCV_CMD_ELEMENT_INDEX = 5;
    public final static int PXCT1_ELEMENT_INDEX = 6;
    public final static int LNCV_ART_L_ELEMENT_INDEX = 7;
    public final static int LNCV_ART_H_ELEMENT_INDEX = 8;
    public final static int LNCV_CVN_L_ELEMENT_INDEX = 9;
    public final static int LNCV_CVN_H_ELEMENT_INDEX = 10;
    public final static int LNCV_MOD_L_ELEMENT_INDEX = 11; // val_l reply is in same positions as mod_l read
    public final static int LNCV_MOD_H_ELEMENT_INDEX = 12; // val_h reply is in same positions as mod_h read
    public final static int LNCV_CMDDATA_ELEMENT_INDEX = 13;
    // Checksum = index 14

    //  helpers for decoding CV format 2 messages (no other OCP_PEER_XFER messages with length 0x0F)
    public final static int LNCV_SRC_ELEMENT_MASK = 0x7f;
    public final static int PXCT1_ELEMENT_VALIDITY_CHECK_MASK = 0x70;
    public final static int PXCT1_ELEMENT_VALIDITY_CHECK_VALUE = 0x10;
    public final static int LNCV_ART_L_ARTL7_CHECK_MASK = 0x01;
    public final static int LNCV_ART_H_ARTH7_CHECK_MASK = 0x02;
    public final static int LNCV_CVN_L_CVNL7_CHECK_MASK = 0x04;
    public final static int LNCV_CVN_H_CVNH7_CHECK_MASK = 0x08;
    public final static int LNCV_MOD_L_MODL7_CHECK_MASK = 0x10;
    public final static int LNCV_MOD_H_MODH7_CHECK_MASK = 0x20;
    public final static int LNCV_CMDDATA_DAT7_CHECK_MASK = 0x40;

    // LocoNet "LNCV format" helper definitions for data
    public final static int LNCV_DATA_START = 0x00;
    public final static int LNCV_DATA_END = 0x40;

    // helpers for decoding LNCV_CMD
    public final static int LNCV_CMD_WRITE = 0x20;
    public final static int LNCV_CMD_READ = 0x21;
    public final static int LNCV_CMD_READ_REPLY = 0x1F; // reply to both LNCV_CMD_READ and ENTER_PROG_MOD (in which case CV0 VAL = MOD)
    // reply to LNCV_CMD_WRITE = LACK


    /**
     * Create a new LncvMessageContents object from a LocoNet message.
     *
     * @param m LocoNet message containing an LNCV Programming Format message
     * @throws IllegalArgumentException if the LocoNet message is not a valid, supported
     *      LNCV Programming Format message
     */
    public LncvMessageContents(LocoNetMessage m)
            throws IllegalArgumentException {

        log.debug("interpreting a LocoNet message - may be an LNCV message");  // NOI18N
        if (!isSupportedLncvMessage(m)) {
            log.debug("interpreting a LocoNet message - is NOT an LNCV message");   // NOI18N
            throw new IllegalArgumentException("LocoNet message is not an LNCV message"); // NOI18N
        }
        src = m.getElement(LNCV_SRC_ELEMENT_INDEX);

        dst_l = m.getElement(LNCV_DST_L_ELEMENT_INDEX);
        dst_h = m.getElement(LNCV_DST_H_ELEMENT_INDEX);
        dst = dst_l + (256 * dst_h);
        log.debug("src={}, dst={}", src, dst); // must use vars for CI

        cmd = m.getElement(LNCV_CMD_ELEMENT_INDEX);

        int svx1 = m.getElement(PXCT1_ELEMENT_INDEX);
        String svx1bin = String.format("%8s", Integer.toBinaryString(svx1)).replace(' ', '0');
        log.debug("SVX1 HIBITS = {}", svx1bin);

        art_l = m.getElement(LNCV_ART_L_ELEMENT_INDEX)
                + (((svx1 & LNCV_ART_L_ARTL7_CHECK_MASK) == LNCV_ART_L_ARTL7_CHECK_MASK)
                ? 0x80 : 0);
        art_h = m.getElement(LNCV_ART_H_ELEMENT_INDEX)
                + (((svx1 & LNCV_ART_H_ARTH7_CHECK_MASK) == LNCV_ART_H_ARTH7_CHECK_MASK)
                ? 0x80 : 0);
        art = art_l + (256 * art_h);

        cvn_l = m.getElement(LNCV_CVN_L_ELEMENT_INDEX)
                + (((svx1 & LNCV_CVN_L_CVNL7_CHECK_MASK) == LNCV_CVN_L_CVNL7_CHECK_MASK)
                ? 0x80 : 0);
        cvn_h = m.getElement(LNCV_CVN_H_ELEMENT_INDEX)
                + (((svx1 & LNCV_CVN_H_CVNH7_CHECK_MASK) == LNCV_CVN_H_CVNH7_CHECK_MASK)
                ? 0x80 : 0);
        cvn = cvn_l + (256 * cvn_h);

        mod_l = m.getElement(LNCV_MOD_L_ELEMENT_INDEX)
                + (((svx1 & LNCV_MOD_L_MODL7_CHECK_MASK) == LNCV_MOD_L_MODL7_CHECK_MASK)
                ? 0x80 : 0);
        mod_h = m.getElement(LNCV_MOD_H_ELEMENT_INDEX)
                + (((svx1 & LNCV_MOD_H_MODH7_CHECK_MASK) == LNCV_MOD_H_MODH7_CHECK_MASK)
                ? 0x80 : 0);
        mod = mod_l + (256 * mod_h);

        cmd_data = m.getElement(LNCV_CMDDATA_ELEMENT_INDEX)
                + (((svx1 & LNCV_CMDDATA_DAT7_CHECK_MASK) == LNCV_CMDDATA_DAT7_CHECK_MASK)
                ? 0x80 : 0);
    }

    /**
     * Check a LocoNet message to determine if it is a valid LNCV Programming Format
     *      message.
     *
     * @param m  LocoNet message to check
     * @return true if LocoNet message m is a supported LNCV Programming Format
     *      message, else false.
     */
    public static boolean isSupportedLncvMessage(LocoNetMessage m) {
        // must be OPC_PEER_XFER or OPC_IMM_PACKET opcode
        if ((m.getOpCode() != LnConstants.OPC_PEER_XFER) && (m.getOpCode() != LnConstants.OPC_IMM_PACKET)) {
            log.debug ("cannot be LNCV message because not OPC_PEER_XFER or OPC_IMM_PACKET");  // NOI18N
            return false;
        }
        
        // length must be 0x0F
        if (m.getElement(1) != LNCV_LENGTH_ELEMENT_VALUE) {
            log.debug ("cannot be LNCV message because not length 0x0F");  // NOI18N
            return false;
            }
        
        // <SRC_ELEMENT> must be correct
        if (m.getElement(LNCV_SRC_ELEMENT_INDEX) != LNCV_CS_SRC_VALUE && m.getElement(LNCV_SRC_ELEMENT_INDEX) != LNCV_LNMODULE_VALUE) {
            log.debug ("cannot be LNCV message because Source not correct");  // NOI18N
            return false;
        }

        // check the combined value
        if (isSupportedLncvCommand(m.getElement(LNCV_CMD_ELEMENT_INDEX))) {
            log.debug("LocoNet message is a supported LNCV Format message");
            return true;
        }
        log.debug("LocoNet message is not a supported LNCV Format message");  // NOI18N
        return false;
    }
    
    /**
     * Compare reply message against a specific LNCV Programming Format message type.
     *
     * @param m  LocoNet message to be verified as an LNCV Programming Format message
     *      with the specified &lt;LNCV_CMD&gt; value
     * @param cvCmd  LNCV Programming Format command to expect
     * @return true if message is an LNCV Programming Format message of the specified &lt;LNCV_CMD&gt;,
     *      else false.
     */
    public static boolean isLnMessageASpecificLncvCommand(LocoNetMessage m, LncvCommand cvCmd) {
        // must be OPC_PEER_XFER or OPC_IMM_PACKET opcode
        if ((m.getOpCode() != LnConstants.OPC_PEER_XFER) && (m.getOpCode() != LnConstants.OPC_IMM_PACKET)) {
            log.debug ("cannot be LNCV message because not OPC_PEER_XFER or OPC_IMM_PACKET");  // NOI18N
            return false;
        }
        
        // length of message must be 0x0F
        if (m.getElement(1) != LNCV_LENGTH_ELEMENT_VALUE) {
            log.debug ("cannot be LNCV message because not length 0x0F");  // NOI18N
            return false;
            }

        // "command_data" identifier must be correct.  Check the rest
        // of the "command_data" identifier
        if ((m.getElement(LNCV_CMDDATA_ELEMENT_INDEX) != LNCV_DATA_END) && (m.getElement(LNCV_CMDDATA_ELEMENT_INDEX) != 0x00)) {
            log.debug ("cannot be LNCV message because CMD_DATA not valid");  // NOI18N
            return false;
        }
        
        // check the <LNCV_CMD> value
        if (isSupportedLncvCommand(m.getElement(LNCV_CMD_ELEMENT_INDEX))) {
            log.debug("LocoNet message is a supported LNCV Format message");  // NOI18N
            if (Objects.equals(extractMessageType(m), cvCmd)) {
                log.debug("LocoNet message is the specified LNCV Format message");  // NOI18N
                return true;
            }
        }
        log.debug("LocoNet message is not a supported LNCV Format message");  // NOI18N
        return false;
    }
    
    /**
     * Interpret a LocoNet message to determine its LNCV Programming Format &lt;LNCV_CMD&gt;.
     * If the message is not an LNCV Programming Format message, returns null.
     *
     * @param m  LocoNet message containing LNCV Programming Format message
     * @return LncvCommand found in the LNCV Programming Format message or null if not found
     */
    public static LncvCommand extractMessageType(LocoNetMessage m) {
        if (isSupportedLncvMessage(m)) {
            int msgCmd = m.getElement(LNCV_CMD_ELEMENT_INDEX);
            for (LncvCommand s: LncvCommand.values()) {
                if (s.getCmd() == msgCmd) {
                    log.debug("LocoNet message has LNCV message command {}", msgCmd);  // NOI18N
                    return s;
                }
            }
        }
        return null;
    }
    
    /**
     * Interpret the LNCV Programming Format message into a human-readable string.
     * 
     * @return String containing a human-readable version of the LNCV Programming 
     *      Format message
     */
    @Override
    public String toString() {
        Locale l = Locale.getDefault();
        return LncvMessageContents.this.toString(l);
    }
    
    /**
     * Interpret the LNCV Programming Format message into a human-readable string.
     * 
     * @param locale  locale to use for the human-readable string
     * @return String containing a human-readable version of the LNCV Programming 
     *      Format message, in the language specified by the Locale, if the 
     *      properties have been translated to that Locale, else in the default
     *      English language.
     */
    public String toString(Locale locale) {
        String returnString;
        log.debug("interpreting an LNCV message - cmd is {}", cmd);  // NOI18N
        
        switch (cmd) {
            case (LNCV_CMD_WRITE): // 0x20
                returnString = Bundle.getMessage(locale, "LNCV_WRITE_INTERPRETED",
                        art,
                        cvn,
                        mod); // mod positions store CV value in ReadReply
                break;

            case (LNCV_CMD_READ): // 0x21
                if (art == LNCV_ALL) {
                    if (cmd_data == 0x00) {
                        log.debug("START {}", cmd_data);
                        returnString = Bundle.getMessage(locale, "LNCV_SESS_START_INTERPRETED");
                        break;
                    } else if (cmd_data == LNCV_DATA_END) {
                        log.debug("END {}", cmd_data);
                        returnString = Bundle.getMessage(locale, "LNCV_SESS_END_INTERPRETED");
                        break;
                    } else {
                        log.debug("Unexpected LNCV Command_Data {}", cmd_data);
                        return  Bundle.getMessage(locale, "LNCV_UNDEFINED_MESSAGE") + "\n";
                    }
                } else {
                    if (cmd_data == LNCV_DATA_END) {
                        returnString = Bundle.getMessage(locale, "LNCV_MOD_PROG_END_INTERPRETED",
                                art,
                                mod);
                        break;
                    } else { // read = module prog start
                        returnString = Bundle.getMessage(locale, "LNCV_READ_INTERPRETED",
                                art,
                                mod,
                                cvn);
                    }
                }
                break;

            case (LNCV_CMD_READ_REPLY):
                returnString = Bundle.getMessage(locale, "LNCV_READ_REPLY_INTERPRETED",
                        art,
                        cvn,
                        mod); // mod positions store CV value in ReadReply
                break;

            default:
                return  Bundle.getMessage(locale, "LNCV_UNDEFINED_MESSAGE") + "\n";
        }

        log.debug("interpreted: {}", returnString);  // NOI18N
        return returnString + "\n"; // NOI18N
    }

    /**
     *
     * @param possibleCmd  integer to be compared to the command list
     * @return  true if the possibleCmd value is one of the supported CV 
     *      Programming Format commands
     */
    public static boolean isSupportedLncvCommand(int possibleCmd) {
        log.debug("CMD = {}", possibleCmd);
        switch (possibleCmd) {
            case (LNCV_CMD_READ):
            case (LNCV_CMD_WRITE):
            case (LNCV_CMD_READ_REPLY):
                return true;
            default:
                return false;
        }
    }    
    
    /**
     * Confirm a message specifies a valid (known) LNCV Programming Format command.
     *
     * @return true if the LNCV message specifies a valid (known) LNCV Programming 
     *      Format command.
     */
    public boolean isSupportedLncvCommand() {
        return isSupportedLncvCommand(cmd);
    }
    
    /**
     *
     * @return true if the LNCV message is an LNCV ReadReply message
     */
    public boolean isSupportedLncvReadReply() {
        return (cmd == LNCV_CMD_READ_REPLY);
    }

    /**
     * Create a LocoNet message containing an LNCV Programming Format message.
     *
     * @param source  source device (&lt;SRC&gt;)
     * @param destination = destination address (for &lt;DST_L&gt; and &lt;DST_H&gt;)
     * @param command  LNCV Programming Format command number (for &lt;LNCV_CMD&gt;)
     * @param articleNum hardware model number/10
     * @param cvNum  LNCV Programming Format 16-bit CV number (for &lt;LNCV_CVN_L&gt; and &lt;LNCV_CVN_H&gt;)
     * @param moduleNum   LNCV Programming Format 16-bit Module number (for &lt;LNCV_MOD_L&gt; and &lt;LNCV_MOD_H&gt;),
     *                    same field is used to return CV Value in READ_REPLY from Module
     * @return LocoNet message for the requested message
     * @throws IllegalArgumentException of command is not a valid LNCV Programming Format &lt;LNCV_CMD&gt; value
     */
    public static LocoNetMessage createLncvMessage (
            int opc,
            int source,
            int destination,
            int command,
            int articleNum,
            int cvNum,
            int moduleNum,
            int cmdData)
        throws IllegalArgumentException {

        if ( ! isSupportedLncvCommand(command)) {
            throw new IllegalArgumentException("Command is not a supported LNCV command"); // NOI18N
        }
        LocoNetMessage m = new LocoNetMessage(LNCV_LENGTH_ELEMENT_VALUE);

        m.setOpCode(opc);
        m.setElement(LNCV_LENGTH_ELEMENT_INDEX, LNCV_LENGTH_ELEMENT_VALUE);
        m.setElement(LNCV_SRC_ELEMENT_INDEX, source);
        m.setElement(LNCV_DST_L_ELEMENT_INDEX, (destination & 0xFF));
        m.setElement(LNCV_DST_H_ELEMENT_INDEX, (destination >> 8));
        log.debug("element {} = command = {}", LNCV_CMD_ELEMENT_INDEX, command);
        m.setElement(LNCV_CMD_ELEMENT_INDEX, command);

        int svx1 = 0x0;
        svx1 = svx1 + (((articleNum & 0x80) == 0x80) ? LNCV_ART_L_ARTL7_CHECK_MASK : 0);
        svx1 = svx1 + (((articleNum & 0x8000) == 0x8000) ? LNCV_ART_H_ARTH7_CHECK_MASK : 0);
        svx1 = svx1 + (((cvNum & 0x80) == 0x80) ? LNCV_CVN_L_CVNL7_CHECK_MASK : 0);
        svx1 = svx1 + (((cvNum & 0x8000) == 0x8000) ? LNCV_CVN_H_CVNH7_CHECK_MASK : 0);
        svx1 = svx1 + (((moduleNum & 0x80) == 0x80) ? LNCV_MOD_L_MODL7_CHECK_MASK : 0);
        svx1 = svx1 + (((moduleNum & 0x8000) == 0x8000) ? LNCV_MOD_H_MODH7_CHECK_MASK : 0);
        svx1 = svx1 + (((cmdData & 0x80) == 0x80) ? LNCV_CMDDATA_DAT7_CHECK_MASK : 0);
        //PXCT1_ELEMENT_VALIDITY_CHECK_VALUE; // TODO check bit 7 = 0
        m.setElement(PXCT1_ELEMENT_INDEX, svx1);

        m.setElement(LNCV_ART_L_ELEMENT_INDEX, (articleNum & 0x7f));
        m.setElement(LNCV_ART_H_ELEMENT_INDEX, ((articleNum >> 8) & 0x7f));
        m.setElement(LNCV_CVN_L_ELEMENT_INDEX, (cvNum & 0x7f));
        m.setElement(LNCV_CVN_H_ELEMENT_INDEX, ((cvNum >> 8) & 0x7f));
        m.setElement(LNCV_MOD_L_ELEMENT_INDEX, (moduleNum & 0x7f));
        log.debug("LNCV MOD_L = {}", m.getElement(LNCV_MOD_L_ELEMENT_INDEX));
        m.setElement(LNCV_MOD_H_ELEMENT_INDEX, ((moduleNum >> 8) & 0x7f));
        log.debug("LNCV MOD_H = {}", m.getElement(LNCV_MOD_H_ELEMENT_INDEX));
        m.setElement(LNCV_CMDDATA_ELEMENT_INDEX, (cmdData & 0x7f));

        log.debug("LocoNet Message ready, cmd = {}", m.getElement(LNCV_CMD_ELEMENT_INDEX));
        return m;
    }

    public int getCvNum() {
        if ((cmd == LncvCommand.LNCV_READ.cmd) ||
                (cmd == LncvCommand.LNCV_WRITE.cmd)) {
            return cvn;
        }
        return -1;
    }

    public int getCvValue() {
        if ((cmd == LncvCommand.LNCV_READ_REPLY.cmd) ||
        (cmd == LncvCommand.LNCV_WRITE.cmd)) {
            return mod;
        }
        return -1;
    }

    public int getLncvArticleNum() {
        if ((cmd == LncvCommand.LNCV_READ.cmd) ||
                (cmd == LncvCommand.LNCV_WRITE.cmd)) {
            return art;
        }
        return -1;
    }

    public int getLncvModuleNum() {
        if ((cmd == LncvCommand.LNCV_READ.cmd)) {
            return mod;
        }
        return -1;
    }
    
    /**
     * Create LocoNet message to start LNCV programming.
     *
     * @return LocoNet message
     */
    public static LocoNetMessage createProgSessionStartCommand() {
        return createLncvMessage(
                LnConstants.OPC_IMM_PACKET,
                0x1,
                0x5,
                LNCV_CMD_READ,
                LNCV_ALL,
                0x0,
                LNCV_ALL,
                0x0);
    }

    /**
     * Create LocoNet message to end LNCV programming.
     *
     * @return LocoNet message
     */
    public static LocoNetMessage createProgSessionEndCommand() {
        return createLncvMessage(
                LnConstants.OPC_PEER_XFER,
                0x1,
                0x5,
                LNCV_CMD_READ,
                LNCV_ALL,
                0x0,
                0x1,
                LNCV_DATA_END);
    }

    /**
     * Create LocoNet message for first query of a CV of this module.
     *
     * @param articleNum  address of the module
     * @param moduleAddress  address of the module
     * @return LocoNet message
     */
    public static LocoNetMessage createModProgStartRequest(int articleNum, int moduleAddress) {
        return createCvReadRequest(
                articleNum,
                moduleAddress,
                0x0); // simply reads first CV0 = module address
    }

    /**
     * Create LocoNet message to leave programming of this module.
     *
     * @param articleNum  address of the module
     * @param moduleAddress  address of the module
     * @return LocoNet message
     */
    public static LocoNetMessage createModProgEndRequest(int articleNum, int moduleAddress) {
        log.debug("MODPROG_END {} message created", moduleAddress);
        return createLncvMessage(
                LnConstants.OPC_PEER_XFER,
                0x1,
                0x5,
                LncvCommand.LNCV_PROG_END.cmd,
                articleNum,
                0x0,
                moduleAddress,
                LNCV_DATA_END);
    }

    /**
     * Create LocoNet message for a write to a CV of this object.
     *
     * @param articleNum  address of the module
     * @param cvNum  CV number to query
     * @param newValue new value to store in CV
     * @return LocoNet message
     */
    public static LocoNetMessage createCvWriteRequest(int articleNum, int cvNum, int newValue) {
        return createLncvMessage(
                LnConstants.OPC_IMM_PACKET,
                0x1,
                0x5,
                LncvCommand.LNCV_WRITE.cmd,
                articleNum,
                cvNum,
                newValue,
                0x0);
    }

    /**
     * Create LocoNet message for a query of a CV of this object.
     *
     * @param articleNum  address of the module
     * @param cvNum  CV number to query
     * @param moduleAddress  address of the module
     * @return LocoNet message
     */
    public static LocoNetMessage createCvReadRequest(int articleNum, int moduleAddress, int cvNum) {
        return createLncvMessage(
                LnConstants.OPC_IMM_PACKET,
                0x1,
                0x5,
                LncvCommand.LNCV_READ.cmd,
                articleNum,
                cvNum,
                moduleAddress,
                0x0);
    }

    /**
     * 
     * @param m  the preceding LocoNet message
     * @return  LocoNet message containing the reply, or null if preceding
     *          message isn't a query
     */
    public static LocoNetMessage createLncvReadReply(LocoNetMessage m) {
        if (!isSupportedLncvMessage(m)) {
            return null;
        }
        if ((m.getElement(5) != LNCV_CMD_READ) || (m.getElement(LNCV_CMDDATA_ELEMENT_INDEX) != LNCV_DATA_START)) {
            return null;
        }
        m.setOpCode(LnConstants.OPC_PEER_XFER);
        m.setElement(LNCV_LENGTH_ELEMENT_INDEX, LNCV_LENGTH_ELEMENT_VALUE);
        m.setElement(LNCV_SRC_ELEMENT_INDEX, LNCV_LNMODULE_VALUE);
        m.setElement(LNCV_DST_L_ELEMENT_INDEX, 0x49);
        m.setElement(LNCV_DST_H_ELEMENT_INDEX, 0x4B);
        m.setElement(5, LNCV_CMD_READ_REPLY);
        // HIBITS last
        m.setElement(7, m.getElement(7));
        m.setElement(8, m.getElement(8));
        m.setElement(9, m.getElement(9));
        m.setElement(10, m.getElement(10));
        m.setElement(11, 0x4); // random cv value_low
        m.setElement(12, 0x4); // random cv value_hi
        m.setElement(13, 0x0);

        m.setElement(6, m.getElement(6)^0x30 ); // HIBITS recalculate (only elements 11 and 12 have changed = HIBITS bits 5 & 6)
        return m;
    }

    public enum LncvCommand { // commands are combined, only 3 different simple commands
        //LNCV_SESS_START (LNCV_CMD_READ),//, LNCV_ALL, LNCV_DATA_START), // CMD=0x21, CmdData=0x0
        //LNCV_SESS_END (LNCV_CMD_READ),//, LNCV_ALL, LNCV_DATA_END), // CMD=0x21, CmdData=0x40
        // LNCV_PROG_START = first LNCV_READ
        // LNCV_PROG_START_REPLY = first LNCV_READ_REPLY
        LNCV_WRITE (LNCV_CMD_WRITE),//, LNCV_DATA_START), // CMD=0x20, CmdData=0x0
        // LNCV_WRITE_REPLY = LACK
        LNCV_READ (LNCV_CMD_READ),//, LNCV_DATA_START), // CMD=0x21, CmdData=0x0
        LNCV_READ_REPLY (LNCV_CMD_READ_REPLY),//, LNCV_DATA_START), // CMD=0x1F, CmdData=0x0
        LNCV_PROG_END (LNCV_CMD_READ);//, LNCV_DATA_END); // CMD=0x21, CmdData=0x40

        public int cmd;
        // public int art;
        // public int cmddata;
        
        LncvCommand(int cmd) {
            this.cmd = cmd;
            // this.art = art;
            // this.cmddata = cmddata;
        }

        int getCmd() {return cmd;}
        
        public static int getCmd(LncvCommand mt) {
            return mt.getCmd();
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LncvMessageContents.class);
    
}

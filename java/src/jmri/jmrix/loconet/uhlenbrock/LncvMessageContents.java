package jmri.jmrix.loconet.uhlenbrock;

import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Objects;

/**
 * Supporting class for Uhlenbrock LocoNet LNCV Programming and Direct Format messaging.
 * Structure adapted from {@link jmri.jmrix.loconet.lnsvf2.LnSv2MessageContents}
 * 
 * Some of the message formats used in this class are Copyright Uhlenbrock.de
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Uhlenbrock.
 *
 * @author Egbert Broerse Copyright (C) 2020, 2021
 */
public class LncvMessageContents {
    private final int opc;
    private final int src;
    private final int dst_l;
    private final int dst_h;
    private final int dst;
    private final int cmd;
    private final int art_l; // D1
    private final int art_h; // D2
    private final int art;
    private final String sArt;
    private final int cvn_l; // D3
    private final int cvn_h; // D4
    private final int cvn;
    private final String sCvn;
    private final int mod_l; // D5
    private final int mod_h; // D6
    private final int mod;
    private final String sMod;
    private final int cmd_data; // D7
    private final LncvCommand command;

    // LocoNet "LNCV format" helper definitions: length byte value for LNCV message
    public final static int LNCV_LENGTH_ELEMENT_VALUE = 0x0f;
    public final static int LNCV_LNMODULE_VALUE = 0x05;
    public final static int LNCV_CS_SRC_VALUE = 0x01;
    public final static int LNCV_PC_SRC_VALUE = 0x08;
    public final static int LNCV_CSDEST_VALUE = 0x4b49;
    public final static int LNCV_ALL = 0xffff; // decimal 65535
    public final static int LNCV_ALL_MASK = 0xff00; // decimal 65535
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

    //  helpers for decoding CV format 2 messages (no other OCP_PEER_XFER messages with length 0x0f)
    public final static int LNCV_SRC_ELEMENT_MASK = 0x7f;
    public final static int PXCT1_ELEMENT_VALIDITY_CHECK_MASK = 0x70;
    public final static int LNCV_ART_L_ARTL7_CHECK_MASK = 0x01;
    public final static int LNCV_ART_H_ARTH7_CHECK_MASK = 0x02;
    public final static int LNCV_CVN_L_CVNL7_CHECK_MASK = 0x04;
    public final static int LNCV_CVN_H_CVNH7_CHECK_MASK = 0x08;
    public final static int LNCV_MOD_L_MODL7_CHECK_MASK = 0x10;
    public final static int LNCV_MOD_H_MODH7_CHECK_MASK = 0x20;
    public final static int LNCV_CMDDATA_DAT7_CHECK_MASK = 0x40;

    // LocoNet "LNCV format" helper definitions for data
    //    public final static int LNCV_DATA_START = 0x00;
    //    public final static int LNCV_DATA_END = 0x40;
    public final static int LNCV_DATA_PROFF_MASK = 0x40;
    public final static int LNCV_DATA_PRON_MASK = 0x80;
    public final static int LNCV_DATA_LED1_MASK = 0xff;
    public final static int LNCV_DATA_LED2_MASK = 0xfe;
    public final static int LNCV_DATA_RO_MASK = 0x01;

    // helpers for decoding LNCV_CMD
    public final static int LNCV_CMD_WRITE = 0x20;
    public final static int LNCV_CMD_READ = 0x21;
    public final static int LNCV_CMD_READ_REPLY = 0x1f; // reply to both LNCV_CMD_READ and ENTER_PROG_MOD (in which case CV0 VAL = MOD)
    // reply to LNCV_CMD_WRITE = LACK, already defined as general LocoNet message type


    /**
     * Create a new LncvMessageContents object from a LocoNet message.
     *
     * @param m LocoNet message containing an LNCV Programming Format message
     * @throws IllegalArgumentException if the LocoNet message is not a valid, supported LNCV Programming Format
     *                                  message
     */
    public LncvMessageContents(LocoNetMessage m) throws IllegalArgumentException {

        //log.debug("interpreting a LocoNet message - may be an LNCV message");  // NOI18N
        if (!isSupportedLncvMessage(m)) {
            //log.debug("interpreting a LocoNet message - is NOT an LNCV message");   // NOI18N
            throw new IllegalArgumentException("LocoNet message is not an LNCV message"); // NOI18N
        }
        this.command = extractMessageType(m);

        opc = m.getOpCode();
        src = m.getElement(LNCV_SRC_ELEMENT_INDEX);

        dst_l = m.getElement(LNCV_DST_L_ELEMENT_INDEX);
        dst_h = m.getElement(LNCV_DST_H_ELEMENT_INDEX);
        dst = dst_l + (256 * dst_h);
        log.debug("src={}, dst={}{}", src, dst, (dst == 19273 ? "=IK" : "")); // must use vars for CI

        cmd = m.getElement(LNCV_CMD_ELEMENT_INDEX);

        int pxct1 = m.getElement(PXCT1_ELEMENT_INDEX);
        String svx1bin = String.format("%8s", Integer.toBinaryString(pxct1)).replace(' ', '0');
        log.debug("PXCT1 HIBITS = {}", svx1bin);

        art_l = m.getElement(LNCV_ART_L_ELEMENT_INDEX) + (((pxct1 & LNCV_ART_L_ARTL7_CHECK_MASK) == LNCV_ART_L_ARTL7_CHECK_MASK) ? 0x80 : 0);
        art_h = m.getElement(LNCV_ART_H_ELEMENT_INDEX) + (((pxct1 & LNCV_ART_H_ARTH7_CHECK_MASK) == LNCV_ART_H_ARTH7_CHECK_MASK) ? 0x80 : 0);
        art = art_l + (256 * art_h);
        sArt = art + "";

        cvn_l = m.getElement(LNCV_CVN_L_ELEMENT_INDEX) + (((pxct1 & LNCV_CVN_L_CVNL7_CHECK_MASK) == LNCV_CVN_L_CVNL7_CHECK_MASK) ? 0x80 : 0);
        cvn_h = m.getElement(LNCV_CVN_H_ELEMENT_INDEX) + (((pxct1 & LNCV_CVN_H_CVNH7_CHECK_MASK) == LNCV_CVN_H_CVNH7_CHECK_MASK) ? 0x80 : 0);
        cvn = cvn_l + (256 * cvn_h);
        sCvn = cvn + "";

        mod_l = m.getElement(LNCV_MOD_L_ELEMENT_INDEX) + (((pxct1 & LNCV_MOD_L_MODL7_CHECK_MASK) == LNCV_MOD_L_MODL7_CHECK_MASK) ? 0x80 : 0);
        mod_h = m.getElement(LNCV_MOD_H_ELEMENT_INDEX) + (((pxct1 & LNCV_MOD_H_MODH7_CHECK_MASK) == LNCV_MOD_H_MODH7_CHECK_MASK) ? 0x80 : 0);
        mod = mod_l + (256 * mod_h);
        sMod = mod + "";

        cmd_data = m.getElement(LNCV_CMDDATA_ELEMENT_INDEX) + (((pxct1 & LNCV_CMDDATA_DAT7_CHECK_MASK) == LNCV_CMDDATA_DAT7_CHECK_MASK) ? 0x80 : 0);
    }

    /**
     * Check a LocoNet message to determine if it is a valid LNCV Programming Format message.
     *
     * @param m LocoNet message to check
     * @return true if LocoNet message m is a supported LNCV Programming Format message, else false.
     */
    public static boolean isSupportedLncvMessage(LocoNetMessage m) {
        // must be OPC_PEER_XFER or OPC_IMM_PACKET opcode
        if ((m.getOpCode() != LnConstants.OPC_PEER_XFER) && (m.getOpCode() != LnConstants.OPC_IMM_PACKET)) {
            //log.debug("cannot be LNCV message because not OPC_PEER_XFER (0xe5) or OPC_IMM_PACKET (0xed)");  // NOI18N
            return false;
        }

        // length must be 0x0f
        if (m.getElement(1) != LNCV_LENGTH_ELEMENT_VALUE) {
            //log.debug("cannot be LNCV message because not length 0x0f");  // NOI18N
            return false;
        }

        // <SRC_ELEMENT> must be correct
        if ((m.getElement(LNCV_SRC_ELEMENT_INDEX) != LNCV_CS_SRC_VALUE) && (m.getElement(LNCV_SRC_ELEMENT_INDEX) != LNCV_LNMODULE_VALUE) && (m.getElement(LNCV_SRC_ELEMENT_INDEX) != LNCV_PC_SRC_VALUE)) {
            //log.debug("cannot be LNCV message because Source not correct");  // NOI18N
            return false;
        }

        // "command_data" identifier must be correct. handled via Enum
        // check the (compound) command element
        int msgData = m.getElement(LNCV_CMDDATA_ELEMENT_INDEX) | (((m.getElement(PXCT1_ELEMENT_INDEX) & LNCV_CMDDATA_DAT7_CHECK_MASK) == LNCV_CMDDATA_DAT7_CHECK_MASK) ? 0x80 : 0);
        return isSupportedLncvCommand(m.getElement(LNCV_CMD_ELEMENT_INDEX), m.getOpCode(), msgData);
    }

    /**
     * Compare reply message against a specific LNCV Programming Format message type.
     *
     * @param m  LocoNet message to be verified as an LNCV Programming Format message with the specified
     *           &lt;LNCV_CMD&gt; value
     * @param lncvCmd LNCV Programming Format command to check against
     * @return true if message is an LNCV Programming Format message of the specified &lt;LNCV_CMD&gt;, else false.
     */
    public static boolean isLnMessageASpecificLncvCommand(LocoNetMessage m, LncvCommand lncvCmd) {
        if (!isSupportedLncvMessage(m)) {
            log.debug("rejected in isLnMessageASpecificLncvCommand");
            return false;
        }
        // compare the <LNCV_CMD> value against cvCmd
        return Objects.equals(extractMessageType(m), lncvCmd);
    }

    /**
     * Interpret a LocoNet message to determine its LNCV compound Programming Format.
     * If the message is not an LNCV Programming/Direct Format message, returns null.
     *
     * @param m LocoNet message containing LNCV Programming Format message
     * @return LncvCommand found in the LNCV Programming Format message or null if not found
     */
    public static LncvCommand extractMessageType(LocoNetMessage m) {
        if (isSupportedLncvMessage(m)) {
            int msgCmd = m.getElement(LNCV_CMD_ELEMENT_INDEX);
            int msgData = m.getElement(LNCV_CMDDATA_ELEMENT_INDEX) | (((m.getElement(PXCT1_ELEMENT_INDEX) & LNCV_CMDDATA_DAT7_CHECK_MASK) == LNCV_CMDDATA_DAT7_CHECK_MASK) ? 0x80 : 0);
            for (LncvCommand c : LncvCommand.values()) {
                if (c.matches(msgCmd, m.getOpCode(), msgData)) {
                    log.debug("LncvCommand match found");  // NOI18N
                    return c;
                }
            }
        }
        return null;
    }

    /**
     * Interpret the LNCV Programming Format message into a human-readable string.
     *
     * @return String containing a human-readable version of the LNCV Programming Format message
     */
    @Override
    public String toString() {
        Locale l = Locale.getDefault();
        return LncvMessageContents.this.toString(l);
    }

    /**
     * Interpret the LNCV Programming Format message into a human-readable string.
     *
     * @param locale locale to use for the human-readable string
     * @return String containing a human-readable version of the LNCV Programming Format message, in the language
     * specified by the Locale, if the properties have been translated to that Locale, else in the default English
     * language.
     */
    public String toString(Locale locale) {
        String returnString;
        //log.debug("interpreting an LNCV message - simple cmd is {}", cmd);  // NOI18N

        switch (this.command) {
            case LNCV_PROG_START:
                if ((art & LNCV_ALL_MASK) == LNCV_ALL_MASK) {
                    returnString = Bundle.getMessage(locale, "LNCV_ALL_PROG_START_INTERPRETED");
                } else if ((mod & LNCV_ALL_MASK) == LNCV_ALL_MASK) {
                    returnString = Bundle.getMessage(locale, "LNCV_ART_PROG_START_INTERPRETED", sArt);
                } else {
                    returnString = Bundle.getMessage(locale, "LNCV_MOD_PROG_START_INTERPRETED", sArt, sMod);
                }
                break;
            case LNCV_PROG_END:
                if ((art & LNCV_ALL_MASK) == LNCV_ALL_MASK) {
                    returnString = Bundle.getMessage(locale, "LNCV_ALL_PROG_END_INTERPRETED");
                } else if ((mod & LNCV_ALL_MASK) == LNCV_ALL_MASK) {
                    returnString = Bundle.getMessage(locale, "LNCV_ART_PROG_END_INTERPRETED", sArt);
                } else {
                    returnString = Bundle.getMessage(locale, "LNCV_MOD_PROG_END_INTERPRETED", sArt, sMod);
                }
                break;
            case LNCV_WRITE: // mod positions store CV value in ReadReply
                returnString = Bundle.getMessage(locale, "LNCV_WRITE_INTERPRETED", sArt, sCvn, sMod);
                break;
            case LNCV_READ:
                // read = module prog start
                returnString = Bundle.getMessage(locale, "LNCV_READ_INTERPRETED", sArt, sMod, sCvn);
                break;
            case LNCV_READ_REPLY: // mod positions store CV value in ReadReply
                returnString = Bundle.getMessage(locale, "LNCV_READ_REPLY_INTERPRETED", sArt, sCvn, sMod);
                break;
            case LNCV_DIRECT_LED1: // CV position contains module address, Value position contains LED 0-15 on/off
                String modBin = String.format("%8s", Integer.toBinaryString(mod)).replace(' ', '0');
                returnString = Bundle.getMessage(locale, "LNCV_DIRECT_INTERPRETED", "1", modBin, sCvn);
                break;
            case LNCV_DIRECT_LED2: // CV position contains module address, Value position contains LED 16-31 on/off
                modBin = String.format("%8s", Integer.toBinaryString(mod)).replace(' ', '0');
                returnString = Bundle.getMessage(locale, "LNCV_DIRECT_INTERPRETED", "2", modBin, sCvn);
                //to16Bits(cvn, true));
                break;
            case LNCV_DIRECT_REPLY: // CV position contains module address, value position = Button on/off message
                returnString = Bundle.getMessage(locale, "LNCV_DIRECT_REPLY_INTERPRETED", sCvn, sMod);
                break;
            default:
                return Bundle.getMessage(locale, "LNCV_UNDEFINED_MESSAGE") + "\n";
        }

        return returnString + "\n"; // NOI18N
    }

    /**
     * Check set of parameters against compound {@link LncvCommand} enum set.
     *
     * @param command LNCV CMD value
     * @param opc     OPC value
     * @param cmdData LNCV cmdData value
     * @return true if the possibleCmd value is one of the supported (simple) LNCV Programming Format commands
     */
    public static boolean isSupportedLncvCommand(int command, int opc, int cmdData) {
        //log.debug("CMD = {}-{}-{}", command, opc, cmdData);
        for (LncvCommand commandToCheck : LncvCommand.values()) {
            if (commandToCheck.matches(command, opc, cmdData)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Confirm a message corresponds with a valid (known) LNCV Programming Format command.
     *
     * @return true if the LNCV message specifies a valid (known) LNCV Programming Format command.
     */
    public boolean isSupportedLncvCommand() {
        return isSupportedLncvCommand(cmd, opc, cmd_data);
    }

    /**
     * @return true if the LNCV message is an LNCV ReadReply message
     */
    public boolean isSupportedLncvReadReply() {
        return (cmd == LNCV_CMD_READ_REPLY);
    }

    /**
     * Create a LocoNet message containing an LNCV Programming Format message.
     *
     * @param opc         Opcode (&lt;OPC&gt;), see LnConstants
     * @param source      source device (&lt;SRC&gt;)
     * @param destination destination address (for &lt;DST_L&gt; and &lt;DST_H&gt;)
     * @param command     LNCV Programming simple command (for &lt;LNCV_CMD&gt;), part of
     *                    complex command {@link LncvCommand}
     * @param articleNum  manufacturer's hardware class/article code as per specs (4 decimal digits)
     * @param cvNum       CV number (for &lt;LNCV_CVN_L&gt; and &lt;LNCV_CVN_H&gt;)
     * @param moduleNum   module address (for &lt;LNCV_MOD_L&gt; and &lt;LNCV_MOD_H&gt;),
     *                    same field is used for CV Value in WRITE to and READ_REPLY from Module
     * @param cmdData     signals programming start/stop: LNCV_DATA_PRON/LNCV_DATA_PROFF
     * @return LocoNet message for the requested instruction
     * @throws IllegalArgumentException of command is not a valid LNCV Programming Format &lt;LNCV_CMD&gt; value
     */
    public static LocoNetMessage createLncvMessage(int opc,
                                                   int source,
                                                   int destination,
                                                   int command,
                                                   int articleNum,
                                                   int cvNum,
                                                   int moduleNum,
                                                   int cmdData) throws IllegalArgumentException {

        if (!isSupportedLncvCommand(command, opc, cmdData)) {
            throw new IllegalArgumentException("Command is not a supported LNCV command"); // NOI18N
        }
        LocoNetMessage m = new LocoNetMessage(LNCV_LENGTH_ELEMENT_VALUE);

        m.setOpCode(opc);
        m.setElement(LNCV_LENGTH_ELEMENT_INDEX, LNCV_LENGTH_ELEMENT_VALUE);
        m.setElement(LNCV_SRC_ELEMENT_INDEX, source);
        m.setElement(LNCV_DST_L_ELEMENT_INDEX, (destination & 0xff));
        m.setElement(LNCV_DST_H_ELEMENT_INDEX, (destination >> 8));
        //log.debug("element {} = command = {}", LNCV_CMD_ELEMENT_INDEX, command);
        m.setElement(LNCV_CMD_ELEMENT_INDEX, command);

        int svx1 = 0x0;
        svx1 = svx1 + (((articleNum & 0x80) == 0x80) ? LNCV_ART_L_ARTL7_CHECK_MASK : 0);
        svx1 = svx1 + (((articleNum & 0x8000) == 0x8000) ? LNCV_ART_H_ARTH7_CHECK_MASK : 0);
        svx1 = svx1 + (((cvNum & 0x80) == 0x80) ? LNCV_CVN_L_CVNL7_CHECK_MASK : 0);
        svx1 = svx1 + (((cvNum & 0x8000) == 0x8000) ? LNCV_CVN_H_CVNH7_CHECK_MASK : 0);
        svx1 = svx1 + (((moduleNum & 0x80) == 0x80) ? LNCV_MOD_L_MODL7_CHECK_MASK : 0);
        svx1 = svx1 + (((moduleNum & 0x8000) == 0x8000) ? LNCV_MOD_H_MODH7_CHECK_MASK : 0);
        //("Fetching hi bit {} of cmdData, value = {}", ((cmdData & 0x80) == 0x80), cmdData);
        svx1 = svx1 + (((cmdData & 0x80) == 0x80) ? LNCV_CMDDATA_DAT7_CHECK_MASK : 0);
        // bit 7 always 0
        m.setElement(PXCT1_ELEMENT_INDEX, svx1);

        m.setElement(LNCV_ART_L_ELEMENT_INDEX, (articleNum & 0x7f));
        m.setElement(LNCV_ART_H_ELEMENT_INDEX, ((articleNum >> 8) & 0x7f));
        m.setElement(LNCV_CVN_L_ELEMENT_INDEX, (cvNum & 0x7f));
        m.setElement(LNCV_CVN_H_ELEMENT_INDEX, ((cvNum >> 8) & 0x7f));
        m.setElement(LNCV_MOD_L_ELEMENT_INDEX, (moduleNum & 0x7f));
        //log.debug("LNCV MOD_L = {}", m.getElement(LNCV_MOD_L_ELEMENT_INDEX));
        m.setElement(LNCV_MOD_H_ELEMENT_INDEX, ((moduleNum >> 8) & 0x7f));
        //log.debug("LNCV MOD_H = {}", m.getElement(LNCV_MOD_H_ELEMENT_INDEX));
        m.setElement(LNCV_CMDDATA_ELEMENT_INDEX, (cmdData & 0x7f));

        //log.debug("LocoNet Message ready, cmd = {}", m.getElement(LNCV_CMD_ELEMENT_INDEX));
        return m;
    }

    /**
     * Create LNCV message from {@link LncvCommand} enum plus specific parameter values.
     *
     * @param source source device (&lt;SRC&gt;)
     * @param destination destination address (for &lt;DST_L&gt; and &lt;DST_H&gt;)
     * @param command one of the composite LncvCommand's
     * @param articleNum manufacturer's hardware class/article code as per specs
     * @param cvNum 16-bit CV number (for &lt;LNCV_CVN_L&gt; and &lt;LNCV_CVN_H&gt;)
     * @param moduleNum module address (for &lt;LNCV_MOD_L&gt; and &lt;LNCV_MOD_H&gt;),
     *                    same field is used for CV Value in WRITE to and READ_REPLY from Module
     * @return LocoNet message for the requested instruction
     */
    public static LocoNetMessage createLncvMessage(int source, int destination, LncvCommand command, int articleNum, int cvNum, int moduleNum) {
        return createLncvMessage(command.getOpc(), source, destination, command.getCmd(), articleNum, cvNum, moduleNum, command.getCmdData());
    }

    public int getCmd() {
        return cmd;
    }

    public int getCvNum() {
        if ((cmd == LncvCommand.LNCV_READ.cmd) ||
                (cmd == LncvCommand.LNCV_WRITE.cmd) ||
                (cmd == LncvCommand.LNCV_READ_REPLY.cmd)) {
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
                (cmd == LncvCommand.LNCV_WRITE.cmd) ||
                (cmd == LncvCommand.LNCV_READ_REPLY.cmd)||
                (cmd == LncvCommand.LNCV_PROG_START.cmd && art != LNCV_ALL)||
                (cmd == LncvCommand.LNCV_PROG_END.cmd && art != LNCV_ALL)) {
            return art;
        }
        return -1;
    }

    public int getLncvModuleNum() {
        if (cmd == LncvCommand.LNCV_READ.cmd ||
                (cmd == LncvCommand.LNCV_PROG_START.cmd && art != LNCV_ALL)||
                (cmd == LncvCommand.LNCV_PROG_END.cmd && art != LNCV_ALL)) {
            return mod;
        }
        return -1;
    }
    
    /**
     * Create LocoNet broadcast message to start LNCV programming.
     *
     * @param articleNum LNCV device type number used as filter to respond. Leave this out to 'broadcast' to
     *                   all connected devices (which works for discovery purpose only)
     * @return LocoNet message
     */
    public static LocoNetMessage createAllProgStartRequest(int articleNum) {
        return createLncvMessage(
                0x1,
                0x5,
                LncvCommand.LNCV_PROG_START,
                (articleNum > -1 ? articleNum : LNCV_ALL),
                0x0,
                LNCV_ALL);
    }

    /**
     * Create LocoNet broadcast message to end LNCV programming.
     * (expect no reply from module)
     *
     * @param articleNum LNCV device type number used as filter to respond. Leave out to 'broadcast' to
     *                   all connected devices (which works for discovery purpose only). Best to use same
     *                   value as used while opening the session.
     * @return LocoNet message
     */
    public static LocoNetMessage createAllProgEndRequest(int articleNum) {
        return createLncvMessage(
                0x1,
                0x5,
                LncvCommand.LNCV_PROG_END,
                (articleNum > -1 ? articleNum : LNCV_ALL),
                0x0,
                LNCV_ALL); // replaces 0x1 from KD notes
    }

    /**
     * Create LocoNet message for first query of a CV of this module.
     *
     * @param articleNum  address of the module
     * @param moduleAddress  address of the module
     * @return LocoNet message
     */
    public static LocoNetMessage createModProgStartRequest(int articleNum, int moduleAddress) {
        return createLncvMessage(
                0x1,
                0x5,
                LncvCommand.LNCV_PROG_START,
                articleNum,
                0x0,
                moduleAddress); // effectively reads first CV0 = module address
    }

    /**
     * Create LocoNet message to leave programming of this module.
     * (expect no reply from module)
     *
     * @param articleNum  address of the module
     * @param moduleAddress  address of the module
     * @return LocoNet message
     */
    public static LocoNetMessage createModProgEndRequest(int articleNum, int moduleAddress) {
        //log.debug("MODPROG_END {} message created", moduleAddress);
        return createLncvMessage(
                0x1,
                0x5,
                LncvCommand.LNCV_PROG_END,
                articleNum,
                0x0,
                moduleAddress);
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
                0x1,
                0x5,
                LncvCommand.LNCV_WRITE,
                articleNum,
                cvNum,
                newValue);
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
                0x1,
                0x5,
                LncvCommand.LNCV_READ,
                articleNum,
                cvNum,
                moduleAddress);
    }

    /* These 2 static methods are used to mock replies to requests from JMRI */

    /**
     * In Hexfile simulation mode, mock a ReadReply message back to the CS.
     *
     * @param m  the LocoNet message to respond to
     * @return  LocoNet message containing the reply, or null if preceding
     *          message isn't a query
     */
    public static LocoNetMessage createLncvReadReply(LocoNetMessage m) {
        if (!isLnMessageASpecificLncvCommand(m, LncvCommand.LNCV_READ)) {
            return null;
        }
        LocoNetMessage reply = new LocoNetMessage(m);
        reply.setOpCode(LnConstants.OPC_PEER_XFER);
        reply.setElement(LNCV_LENGTH_ELEMENT_INDEX, LNCV_LENGTH_ELEMENT_VALUE);

        reply.setElement(LNCV_DST_L_ELEMENT_INDEX, (reply.getElement(LNCV_SRC_ELEMENT_INDEX) == LNCV_CS_SRC_VALUE ? 0x49 : reply.getElement(LNCV_SRC_ELEMENT_INDEX)));
        reply.setElement(LNCV_DST_H_ELEMENT_INDEX, (reply.getElement(LNCV_SRC_ELEMENT_INDEX) == LNCV_CS_SRC_VALUE ? 0x4b : 0x00));

        // set SRC after reading old value to determine DST above
        reply.setElement(LNCV_SRC_ELEMENT_INDEX, LNCV_LNMODULE_VALUE);
        reply.setElement(5, LNCV_CMD_READ_REPLY);
        // HIBITS handled last
        reply.setElement(7, reply.getElement(7));
        reply.setElement(8, reply.getElement(8));
        reply.setElement(9, reply.getElement(9));
        reply.setElement(10, reply.getElement(10));
        if (reply.getElement(9) != 0 || reply.getElement(10) != 0) { // if CV=0, keep cv value as is, it was passed in as the module address
            reply.setElement(LNCV_MOD_L_ELEMENT_INDEX, 0x8); // random cv value_low
            reply.setElement(LNCV_MOD_H_ELEMENT_INDEX, 0x1); // random cv value_hi
            reply.setElement(PXCT1_ELEMENT_INDEX, reply.getElement(PXCT1_ELEMENT_INDEX)^0x60); // HIBITS recalculate (only elements 11-12 have changed = HIBITS bits 5 & 6)
        }
        reply.setElement(13, 0x0);

        return reply;
    }

    /**
     * In Hexfile simulation mode, mock a ProgStart reply message back to the CS.
     *
     * @param m the LocoNet message to respond to
     * @return  LocoNet message containing the reply, or null if preceding
     *          message isn't a query
     */
    public static LocoNetMessage createLncvProgStartReply(LocoNetMessage m) {
        if (!isLnMessageASpecificLncvCommand(m, LncvCommand.LNCV_PROG_START)) {
            return null;
        }
        LncvMessageContents lmc = new LncvMessageContents(m);
        log.debug("request to article {}", lmc.getLncvArticleNum());
        LocoNetMessage forward = new LocoNetMessage(m);
        forward.setElement(LncvMessageContents.LNCV_CMDDATA_ELEMENT_INDEX, 0x00); // correct CMDDATA for ReadRequest
        forward.setElement(LncvMessageContents.PXCT1_ELEMENT_INDEX, m.getElement(PXCT1_ELEMENT_INDEX)^0x40); // together with this HIBIT
        if (lmc.getLncvArticleNum() == LNCV_ALL) { // mock a certain device
            log.debug("art ALL");
            forward.setElement(LncvMessageContents.LNCV_ART_L_ELEMENT_INDEX, 0x29); // article number 5033
            forward.setElement(LncvMessageContents.LNCV_ART_H_ELEMENT_INDEX, 0x13);
            forward.setElement(LncvMessageContents.PXCT1_ELEMENT_INDEX, 0x01); // hibits to go with 5033
        }
        if (lmc.getLncvModuleNum() == LNCV_ALL) { // mock a certain address
            log.debug("mod ALL");
            forward.setElement(LncvMessageContents.LNCV_MOD_L_ELEMENT_INDEX, 0x3); // address value 3
            forward.setElement(LncvMessageContents.LNCV_MOD_H_ELEMENT_INDEX, 0x0);
        }
        return LncvMessageContents.createLncvReadReply(forward);
    }

    /**
     * Create LocoNet message to set aseries of Track-Control module display LEDs.
     *
     * @param moduleAddress  address of the module
     * @param ledValue  CV number to query
     * @param range2 true if intended for LED2 Command (leds 16-31), fasle for LED1 (0-15)
     * @return LocoNet message
     */
    public static LocoNetMessage createDirectWriteRequest(int moduleAddress, int ledValue, boolean range2) {
        return createLncvMessage(
                LNCV_PC_SRC_VALUE,
                0x5,
                (range2 ? LncvCommand.LNCV_DIRECT_LED2 : LncvCommand.LNCV_DIRECT_LED1),
                6900,
                moduleAddress, // special: CV position [D3-D4] contains the module address
                ledValue);
    }

    /**
     * LNCV Commands mapped to unique sets of 3 parts in message. LNCV knows only 3 simple &lt;CMD&gt; values.
     */
    public enum LncvCommand { // commands mapped to 3 values in message, LNCV knows only 3 simple commands
        LNCV_WRITE (LNCV_CMD_WRITE, LnConstants.OPC_IMM_PACKET, 0x00), // CMD=0x20, CmdData=0x0
        // LNCV_WRITE_REPLY = LACK
        LNCV_READ (LNCV_CMD_READ, LnConstants.OPC_IMM_PACKET, 0x00), // CMD=0x21, CmdData=0x0
        LNCV_READ_REPLY (LNCV_CMD_READ_REPLY, LnConstants.OPC_PEER_XFER, 0x00), // CMD=0x1f, CmdData=0x0
        LNCV_PROG_START (LNCV_CMD_READ, LnConstants.OPC_IMM_PACKET, LNCV_DATA_PRON_MASK), // CMD=0x21, CmdData=0x80
        LNCV_PROG_END (LNCV_CMD_READ, LnConstants.OPC_PEER_XFER, LNCV_DATA_PROFF_MASK), // CMD=0x21, CmdData=0x40
        LNCV_DIRECT_LED1 (LNCV_CMD_WRITE, LnConstants.OPC_IMM_PACKET, LNCV_DATA_LED1_MASK), // CMD=0x20, CmdData=0xff
        LNCV_DIRECT_LED2 (LNCV_CMD_WRITE, LnConstants.OPC_IMM_PACKET, LNCV_DATA_LED2_MASK), // CMD=0x20, CmdData=0xfe
        LNCV_DIRECT_REPLY (LNCV_CMD_READ_REPLY, LnConstants.OPC_PEER_XFER, LNCV_DATA_LED1_MASK); // CMD=0x1f, CmdData=0xff

        private final int cmd;
        private final int opc;
        private final int cmddata;
        
        LncvCommand(int cmd, int opc, int cmddata) {
            this.cmd = cmd;
            this.opc = opc;
            this.cmddata = cmddata;
        }

        int getCmd() {return cmd;}
        int getOpc() {return opc;}
        int getCmdData() {return cmddata;}
        
        public static int getCmd(LncvCommand mt) {
            return mt.getCmd();
        }

        public Boolean matches(int matchCommand, int matchOpc, int matchData) {
            //log.debug("CMD ENUM command {}={}? {}", matchCommand, cmd, (matchCommand == cmd));
            //log.debug("CMD ENUM opc {}={}? {}", matchOpc, opc, (matchOpc == opc));
            //log.debug("CMD ENUM commanddata {}={}? {}", matchData, cmddata, (matchData == cmddata));
            return ((matchCommand == cmd) && (matchOpc == opc) && (matchData == cmddata));
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LncvMessageContents.class);
    
}

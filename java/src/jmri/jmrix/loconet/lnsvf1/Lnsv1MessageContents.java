package jmri.jmrix.loconet.lnsvf1;

import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.messageinterp.LocoNetMessageInterpret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Objects;

/**
 * Supporting class for LocoNet SV Programming Format 1 (LocoIO) messaging.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 * <p>
 * Uses the LOCONETSV1MODE programming mode.
 * <p>
 * Uses LnProgrammer LOCOIO_PEER_CODE_SV_VER1 message format, comparable to DecoderPro LOCONETSV1MODE
 * The DecoderPro decoder definition is recommended for all LocoIO versions. Requires JMRI 4.12 or later.
 *
 * @see jmri.jmrix.loconet.LnOpsModeProgrammer#message(LocoNetMessage)
 *
 * Programming SV's
 * <p>
 * The SV's in a LocoIO hardware module can be programmed using LocoNet OPC_PEER_XFER messages.
 * <p>
 * Commands for setting SV's:
 * <p>
 * PC to LocoIO LocoNet message (OPC_PEER_XFER)
 * <pre><code>
 * Code LNSV1_SV_READ       LNSV1_SV_WRITE
 * 0xE5 OPC_PEER_XFER
 * 0x10 2nd part of OpCode
 * SRCL 0x50                0x50 // low address of LocoBuffer, high address assumed 1
 * DSTL LocoIO address
 * DSTH always 0x01
 * PXCT1
 * D1 LNSV1_SV_READ _or_    LNSV1_SV_WRITE // Read/Write command
 * D2 SV number             SV number
 * D3 0x00                  0x00
 * D4 0x00                  New value byte to Write
 * PXCT2
 * D5 LocoIO sub-address    LocoIO sub-address
 * D6 0x00                  0x00
 * D7 0x00                  0x00
 * D8 0x00                  0x00
 * CHK Checksum             Checksum
 * </code></pre>
 *
 * LocoIO to PC reply message (OPC_PEER_XFER)
 * <pre><code>
 * Code LNSV1__SV_READ      LNSV1__SV_WRITE
 * 0xE5 OPC_PEER_XFER
 * 0x10 2nd part of OpCode
 * SRCL LocoIO low address  LocoIO low address
 * DSTL 0x50                0x50 // address of LocoBuffer
 * DSTH always 0x01         always 0x01
 * PXCT1 MSB SV + version   // High order bits of SV and LocoIO version
 * D1 LNSV1_READ _or_       LNSV1_WRITE // Copy of original Command
 * D2 SV number requested   SV number requested
 * D3 LSBs LocoIO version   // Lower 7 bits of LocoIO version
 * D4 0x00                  0x00
 * PXCT2 MSB Requested Data // High order bits of written cvValue
 * D5 LocoIO Sub-address    LocoIO Sub-address
 * D6 Requested Data        0x00
 * D7 Requested Data + 1    0x00
 * D8 Requested Data + 2    Written cvValue confirmed
 * CHK Checksum             Checksum
 * </code></pre>
 *
 * @author John Plocher 2006, 2007
 * @author B. Milhaupt Copyright (C) 2015
 * @author E. Broerse Copyright (C) 2025
 */
public class Lnsv1MessageContents {
    public static final int LNSV1_BROADCAST_ADDRESS = 0x00; // LocoIO broadcast (addr_H = 1)
    public static final int LNSV1_LOCOBUFFER_ADDRESS = 0x50; // LocoBuffer reserved address (addr_H = 1)
    public static final int LNSV1_PEER_CODE_SV_VER0 = 0x00; // observed in read and write replies from LocoIO
    public static final int LNSV1_PEER_CODE_SV_VER1 = 0x08; // for read and write requests, not for replies

    private final int src_l;
    private final int sv_cmd;
    private final int dst_l;
    private final int dst_h;
    private final int sub_adr;
    private final int sv_num;
    private final int vrs;
    private final int d4;
    private final int d6;
    private final int d7;
    private final int d8;

    // Helper to calculate LocoIO Sensor address from returned data is in LocoNetMessage

    // LocoNet "SV 1 format" helper definitions: indexes into the LocoNet message
    public final static int SV1_SV_SRC_L_ELEMENT_INDEX = 2;
    public final static int SV1_SV_DST_L_ELEMENT_INDEX = 3;
    public final static int SV1_SV_DST_H_ELEMENT_INDEX = 4;
    public final static int SV1_SVX1_ELEMENT_INDEX = 5;
    public final static int SV1_SV_CMD_ELEMENT_INDEX = 6;
    public final static int SV1_SVX2_ELEMENT_INDEX = 10;

    // decoding SV format 1 messages (versus other OCP_PEER_XFER messages with length 0x10) uses m.getPeerXfrData()
    public final static int SV1_SVX1_ELEMENT_VALIDITY_CHECK_MASK = 0x70;
    public final static int SV1_SVX1_ELEMENT_VALIDITY_CHECK_VALUE = 0x00;
    public final static int SV1_SVX2_ELEMENT_VALIDITY_CHECK_MASK = 0x70;
    public final static int SV0_SVX2_ELEMENT_VALIDITY_CHECK_VALUE = 0x10; // LNSV0 mask
    public final static int SV1_SVX2_ELEMENT_VALIDITY_CHECK_VALUE = 0x00; // LNSV1 mask

    // helpers for decoding SV_CMD, compare to ENUM Sv1Command below
    public final static int SV_CMD_WRITE_ONE = 0x01;
    public final static int SV_CMD_READ_ONE = 0x02;

    /**
     * Create a new Lnsv1MessageContents object from a LocoNet message.
     *
     * @param m LocoNet message containing an SV Programming Format 1 message
     * @throws IllegalArgumentException if the LocoNet message is not a valid, supported
     *      SV Programming Format 1 message
     */
    public Lnsv1MessageContents(LocoNetMessage m)
            throws IllegalArgumentException {

        log.debug("interpreting a LocoNet message - may be an SV1 message");  // NOI18N
        if (!isSupportedSv1Message(m)) {
            log.debug("interpreting a LocoNet message - is NOT an SV1 message");   // NOI18N
            throw new IllegalArgumentException("LocoNet message is not an SV1 message"); // NOI18N
        }
        src_l = m.getElement(SV1_SV_SRC_L_ELEMENT_INDEX);
        dst_l = m.getElement(SV1_SV_DST_L_ELEMENT_INDEX);
        dst_h = m.getElement(SV1_SV_DST_H_ELEMENT_INDEX); // should always be 0x01

        int[] d = m.getPeerXfrData();
        sv_cmd = d[0];
        sv_num = d[1];
        vrs = d[2];
        d4 = d[3];
        sub_adr = d[4];
        d6 = d[5];
        d7 = d[6];
        d8 = d[7];
    }

    /**
     * Check a LocoNet message to determine if it is a valid SV Programming Format 1
     *      message.
     *
     * @param m  LocoNet message to check
     * @return true if LocoNet message m is a supported SV Programming Format 1
     *      message, else false.
     */
    public static boolean isSupportedSv1Message(LocoNetMessage m) {
        // must be OPC_PEER_XFER opcode
        if (m.getOpCode() != LnConstants.OPC_PEER_XFER) {
            log.debug ("cannot be SV1 message because not OPC_PEER_XFER");  // NOI18N
            return false;
        }

        // Element 1 must be 0x10
        if (m.getElement(1) != 0x10) {
            log.debug ("cannot be SV1 message because elem. 1 not 0x10");  // NOI18N
            return false;
        }

        if (m.getElement(SV1_SV_DST_H_ELEMENT_INDEX) != 1) {
            log.debug ("cannot be SV1 message because elem. 4 not 0x01");  // NOI18N
            return false;
        }

        // Check PXCT1
        if ((m.getElement(SV1_SVX1_ELEMENT_INDEX)
                & SV1_SVX1_ELEMENT_VALIDITY_CHECK_MASK)
                != SV1_SVX1_ELEMENT_VALIDITY_CHECK_VALUE) {
            log.debug ("cannot be SV1 message because SVX1 upper nibble wrong");  // NOI18N
            return false;
        }
        // Check PXCT2
        if ((m.getElement(SV1_SVX2_ELEMENT_INDEX) // SV0 Broadcast/Write from LocoBuffer
                & SV1_SVX2_ELEMENT_VALIDITY_CHECK_MASK)
                != SV0_SVX2_ELEMENT_VALIDITY_CHECK_VALUE) {
            if ((m.getElement(SV1_SVX2_ELEMENT_INDEX) // SV1 Read/Write reply from LocoIO
                    & SV1_SVX2_ELEMENT_VALIDITY_CHECK_MASK) != SV1_SVX2_ELEMENT_VALIDITY_CHECK_VALUE) {
                log.debug("cannot be SV1 message because SVX2 upper nibble wrong: {}", // extra CHECK_VALUE for replies?
                        m.getElement(SV1_SVX2_ELEMENT_INDEX) & SV1_SVX2_ELEMENT_VALIDITY_CHECK_MASK);  // NOI18N
                return false;
            }
        }

        // check the <SV_CMD> value
        if (isSupportedSv1Command(m.getElement(SV1_SV_CMD_ELEMENT_INDEX))) {
            log.debug("LocoNet message is a supported SV Format 1 message");
            return true;
        }
        log.debug("LocoNet message is not a supported SV Format 1 message");  // NOI18N
        return false;
    }

    /**
     * Compare reply message against a specific SV Programming Format 1 message type.
     *
     * @param m  LocoNet message to be verified as an SV Programming Format 1 message
     *      with the specified &lt;SV_CMD&gt; value
     * @param svCmd  SV Programming Format 1 command to expect
     * @return true if message is an SV Programming Format 1 message of the specified &lt;SV_CMD&gt;,
     *      else false.
     */
    public static boolean isLnMessageASpecificSv1Command(LocoNetMessage m, Sv1Command svCmd) {
        // must be OPC_PEER_XFER opcode
        if (m.getOpCode() != LnConstants.OPC_PEER_XFER) {
            log.debug ("cannot be SV1 message because not OPC_PEER_XFER");  // NOI18N
            return false;
        }

        // length of OPC_PEER_XFER must be 0x10
        if (m.getElement(1) != 0x10) {
            log.debug ("cannot be SV1 message because not length 0x10");  // NOI18N
            return false;
        }

        // The upper nibble of PXCT1 must be 0, and the upper nibble of PXCT2 must be 1 or 2.
        // Check PCX1
        if ((m.getElement(SV1_SVX1_ELEMENT_INDEX)
                & SV1_SVX1_ELEMENT_VALIDITY_CHECK_MASK)
                != SV1_SVX1_ELEMENT_VALIDITY_CHECK_VALUE) {
            log.debug ("cannot be SV1 message because SVX1 upper nibble wrong");  // NOI18N
            return false;
        }
        // Check PCX2
        if ((m.getElement(SV1_SVX2_ELEMENT_INDEX) // SV0 Broadcast/Write from LocoBuffer
                & SV1_SVX2_ELEMENT_VALIDITY_CHECK_MASK)
                != SV0_SVX2_ELEMENT_VALIDITY_CHECK_VALUE) {
            if ((m.getElement(SV1_SVX2_ELEMENT_INDEX) // SV1 Read/Write reply from LocoIO
                    & SV1_SVX2_ELEMENT_VALIDITY_CHECK_MASK)
                    != SV1_SVX2_ELEMENT_VALIDITY_CHECK_VALUE) {
                log.debug ("cannot be SV1 message because SVX2 upper nibble wrong {}",
                        m.getElement(SV1_SVX2_ELEMENT_INDEX) & SV1_SVX2_ELEMENT_VALIDITY_CHECK_MASK);  // NOI18N
                return false;
            }
        }

        // check the <SV_CMD> value
        if (isSupportedSv1Command(m.getElement(SV1_SV_CMD_ELEMENT_INDEX))) {
            log.debug("LocoNet message is a supported SV Format 1 message");  // NOI18N
            if (Objects.equals(extractMessageType(m), svCmd)) {
                log.debug("LocoNet message is the specified SV Format 1 message");  // NOI18N
                return true;
            }
        }
        log.debug("LocoNet message is not a supported SV Format 1 message");  // NOI18N
        return false;
    }

    /**
     * Interpret a LocoNet message to extract its SV Programming Format 1 &lt;SV_CMD&gt;.
     * If the message is not an SV Programming Format 1 message, returns null.
     *
     * @param m  LocoNet message containing SV Programming Format 1 message
     * @return Sv1Command found in the SV Programming Format 1 message or null if not found
     */
    public static Sv1Command extractMessageType(LocoNetMessage m) {
        if (isSupportedSv1Message(m)) {
            int msgCmd = m.getPeerXfrData()[0];
            for (Sv1Command s: Sv1Command.values()) {
                if (s.getCmd() == msgCmd) {
                    log.debug("LocoNet message has SV1 message command {}", msgCmd);  // NOI18N
                    return s;
                }
            }
        }
        return null;
    }

    /**
     * Interpret a LocoNet message to extract its SV Programming Format 1 &lt;SV_CMD&gt;.
     * If the message is not an SV Programming Format 1 message, return null.
     *
     * @param m  LocoNet message containing SV Programming Format 1 version field
     * @return Version found in the SV Programming Format 1 message or -1 if not found
     */
    public static int extractMessageVersion(LocoNetMessage m) {
        if (isSupportedSv1Message(m)) {
            int v = m.getPeerXfrData()[2];
            log.debug("LocoNet LNSV1 message contains version {}", v);  // NOI18N
            return (v > 0 ? v : -1);
        }
        return -1;
    }

    /**
     * Interpret the SV Programming Format 1 message into a human-readable string.
     *
     * @return String containing a human-readable version of the SV Programming
     *      Format 1 message
     */
    @Override
    public String toString() {
        Locale l = Locale.getDefault();
        return Lnsv1MessageContents.this.toString(l);
    }

    /**
     * Interpret the SV Programming Format 1 message into a human-readable string.
     *
     * @param locale  locale to use for the human-readable string
     * @return String containing a human-readable version of the SV Programming
     *      Format 1 message, in the language specified by the Locale if the
     *      properties have been translated to that Locale, else in the default
     *      English language.
     */
    public String toString(Locale locale) {
        String returnString;
        log.debug("interpreting an SV1 message - sv_cmd is {}", sv_cmd);  // NOI18N

        //  use Integer.toHexString(i) and/or String.format("0x%02X", i))
        switch (sv_cmd) {
            case (SV_CMD_WRITE_ONE):
                if (src_l == 0x50) {
                    if (dst_l == 0) {
                        returnString = Bundle.getMessage(locale, "SV1_WRITE_ALL_INTERPRETED",
                                (sv_num == 1 ? "" : "sub"), // makes sub+address // NOI18N
                                toHexStr(sv_num),
                                toHexStr(d4));
                    } else if (dst_l == 0x50) {
                        returnString = Bundle.getMessage(locale, "SV1_WRITE_LB_INTERPRETED",
                                toHexStr(sv_num),
                                toHexStr(d4),
                                (vrs > 0) ? " Firmware rev " + LocoNetMessageInterpret.dotme(vrs) : ""); // in test, useful?
                    } else {
                        returnString = Bundle.getMessage(locale, "SV1_WRITE_INTERPRETED",
                                toHexComposite(dst_l, sub_adr),
                                toHexStr(sv_num),
                                toHexStr(d4),
                                (vrs > 0) ? " Firmware rev " + LocoNetMessageInterpret.dotme(vrs) : ""); // NOI18N
                    }
                } else {
                    returnString = Bundle.getMessage(locale, "SV1_WRITE_REPLY_INTERPRETED",
                            toHexComposite(src_l, sub_adr),
                            toHexStr(sv_num),
                            toHexStr(d8),
                            (vrs > 0) ? " Firmware rev " + LocoNetMessageInterpret.dotme(vrs) : ""); // NOI18N
                }
                break;

            case (SV_CMD_READ_ONE):
                if (src_l == 0x50) {
                    if (dst_l == 0) {
                        returnString = Bundle.getMessage(locale, "SV1_PROBE_ALL_INTERPRETED");
                    } else if (dst_l == 0x50) {
                        returnString = Bundle.getMessage(locale, "SV1_READ_LB_INTERPRETED",
                                toHexStr(sv_num),
                                (vrs > 0) ? " Firmware rev " + LocoNetMessageInterpret.dotme(vrs) : ""); // in test, useful?
                    } else {
                        returnString = Bundle.getMessage(locale, "SV1_READ_INTERPRETED",
                                toHexComposite(dst_l, sub_adr),
                                toHexStr(sv_num),
                                (vrs > 0) ? " Firmware rev " + LocoNetMessageInterpret.dotme(vrs) : "");
                    }
                } else {
                    returnString = Bundle.getMessage(locale, "SV1_READ_REPLY_INTERPRETED",
                            toHexComposite(src_l, sub_adr),
                            toHexStr(sv_num),
                            toHexStr(d6),
                            (vrs > 0) ? " Firmware rev " + LocoNetMessageInterpret.dotme(vrs) : ""); // NOI18N
                }
                break;

            default:
                return Bundle.getMessage(locale, "SV1_UNDEFINED_MESSAGE") + "\n";
        }

        log.debug("interpreted: {}", returnString);  // NOI18N
        return returnString + "\n"; // NOI18N
    }

    private static final String HEX_FORMAT = "0x%02X";

    /**
     * Format byte for decimal + (optional) hex display
     *
     */
    public static String toHexStr(int value) {
        if (value > 9) {
            return value + " (" + String.format(HEX_FORMAT, value) + ")";
        } else {
            return String.valueOf(value);
        }
    }

    /**
     * Format byte for hex display
     *
     */
    public static String toHexComposite(int low, int high) {
        String res = String.valueOf(low);
        if (high == 0) {
            if (low > 9) {
                res += " (" + String.format(HEX_FORMAT, low) + ")";
            }
            return res;
        }
        res += "/" + high;
        if (low > 9 || high > 9) {
            res += " (";
            res += (low > 9 ? String.format(HEX_FORMAT, low) : low);
            res += "/";
            res += (high > 9 ? String.format(HEX_FORMAT, high) : high);
            res += ")";
        }
        return res;
    }

    /**
     *
     * @param possibleCmd  integer to be compared to the command list
     * @return  true if the possibleCmd value is one of the supported SV
     *      Programming Format 1 commands
     */
    public static boolean isSupportedSv1Command(int possibleCmd) {
        switch (possibleCmd) {
            case (SV_CMD_WRITE_ONE):
            case (SV_CMD_READ_ONE):
                return true;
            default:
                return false;
        }
    }

    /**
     * Confirm a message specifies a valid (known) SV Programming Format 1 command.
     *
     * @return true if the SV1 message specifies a valid (known) SV Programming
     *      Format 1 command.
     */
    public boolean isSupportedSv1Command() {
        return isSupportedSv1Command(sv_cmd);
    }

    /**
     *
     * @return true if the SV1 message is a SV1 Read One Reply message
     */
    public boolean isSupportedSv1ReadOneReply() {
        return (sv_cmd == SV_CMD_READ_ONE && src_l != 0x50 && vrs != 0);
    }

    /**
     * Get the data from a SVs READ_ONE Reply message. May also be used to
     * return the effective SV value reported in an SV1 WRITE_ONE Reply message (or is that returned in d8?).
     *
     * @return the {@code <D6>} value from the SV1 message
     */
    public int getSingleReadReportData() {
        return d6;
    }

    public int getSrcL() {
        return src_l;
    }

    public int getDstL() {
        return dst_l;
    }

    /** Used to check message. LNSV1 messages do not use the DST_H field for high address */
    public int getDstH() {
        return dst_h;
    }

    /** Not returning a valid address because LNSV1 messages do not use the DST_H field for high address.
     * and a composite address is not used.
     * - LocoBuffer subaddress is always 1.
     * - LocoIO subaddress is stored and fetched from PEER_XFER element SV1_SV_SUBADR_ELEMENT_INDEX (11).
     * - JMRI LocoIO decoder address as stored in the Roster is calculated as a 14-bit number
     * in jmri.jmrix.loconet.swing.lnsv1prog.Lnsv1ProgPane
     */
    public int getDestAddr() {
        return -1;
    }

    public int getSubAddress() {
        return sub_adr;
    }

    public int getCmd() {
        return sv_cmd;
    }

    public int getSvNum() {
        if ((sv_cmd == Sv1Command.SV1_READ.sv_cmd) ||
                (sv_cmd == Sv1Command.SV1_WRITE.sv_cmd)) {
            return sv_num;
        }
        return -1;
    }

    public int getSvValue() {
        if (sv_cmd == Sv1Command.SV1_READ.sv_cmd) {
            if (vrs > 0) { // Read reply
                return d6;
            } else {
                return d4; // Read request
            }
        } else if (sv_cmd == Sv1Command.SV1_WRITE.sv_cmd) {
            if (vrs > 0) {
                return d8; // Write reply
            } else {
                return d4; // Write request
            }
        }
        return -1;
    }

    public int getVersionNum() {
        if (vrs > 0) {
            return vrs;
        }
        return -1;
    }

    /**
     * Get the d4 value
     * @return d4 element contents
     */
    public int getSv1D4() {
        return d4;
    }

    /**
     * Get the d6 value
     * @return d6 element contents
     */
    public int getSv1D6() {
        return d6;
    }

    /**
     * Get the d7 value
     * @return d7 element contents
     */
    public int getSv1D7() {
        return d7;
    }

    /**
     * Get the d8 value
     * @return d8 element contents
     */
    public int getSv1D8() {
        return d8;
    }

    // ****** Create LNSV1 messages ***** //

    /**
     * Create a LocoNet message containing an SV Programming Format 0 message.
     * Used only to simulate replies from LocoIO. Uses LNSV1_PEER_CODE_SV_VER0.
     * <p>
     * See Programmer message code in {@link jmri.jmrix.loconet.LnOpsModeProgrammer} loadSV1MessageFormat
     *
     * @param source  source device address (for &lt;SRC_L&gt;)
     * @param destination = SV format 1 7-bit destination address (for &lt;DST_L&gt;)
     * @param subAddress = SV format 1 7-bit destination subaddress (for &lt;DST_H&gt;)
     * @param command  SV Programming Format 1 command number (for &lt;SV_CMD&gt;)
     * @param svNum  SV Programming Format 1 8-bit SV number
     * @param newVal (d4)  SV first 8-bit data value to write (for &lt;D4&gt;)
     * @param version  Programming Format 1 8-bit firmware version number; 0 in request,{@literal >0} in replies
     * @param d6  second 8-bit data value (for &lt;D6&gt;)
     * @param d7  third 8-bit data value (for &lt;D7&gt;)
     * @param d8  fourth 8-bit data value (for &lt;D8&gt;)
     * @return LocoNet message for the requested message
     * @throws IllegalArgumentException if command is not a valid SV Programming Format 1 &lt;SV_CMD&gt; value
     */
    public static LocoNetMessage createSv0Message (
            int source,
            int destination,
            int subAddress,
            int command,
            int svNum,
            int version,
            int newVal,
            int d6,
            int d7,
            int d8)
            throws IllegalArgumentException {

        if (! isSupportedSv1Command(command)) {
            throw new IllegalArgumentException("Command is not a supported SV1 command"); // NOI18N
        }
        int[] contents = {command, svNum, version, newVal, subAddress, d6, d7, d8};
        log.debug("createSv1Message src={} dst={} subAddr={} data[]={}", source, destination, subAddress, contents);
        return LocoNetMessage.makePeerXfr(
                source,
                destination,
                contents,
                LNSV1_PEER_CODE_SV_VER0
        );
    }

    /**
     * Create a LocoNet message containing an SV Programming Format 1 message.
     * <p>
     * See Programmer message code in {@link jmri.jmrix.loconet.LnOpsModeProgrammer} loadSV1MessageFormat
     *
     * @param source  source device address (for &lt;SRC_L&gt;)
     * @param destination = SV format 1 7-bit destination address (for &lt;DST_L&gt;)
     * @param subAddress = SV format 1 7-bit destination subaddress (for &lt;DST_H&gt;)
     * @param command  SV Programming Format 1 command number (for &lt;SV_CMD&gt;)
     * @param svNum  SV Programming Format 1 8-bit SV number
     * @param newVal (d4)  SV first 8-bit data value to write (for &lt;D4&gt;)
     * @param version  Programming Format 1 8-bit firmware version number; 0 in request,{@literal >0} in replies
     * @param d6  second 8-bit data value (for &lt;D6&gt;)
     * @param d7  third 8-bit data value (for &lt;D7&gt;)
     * @param d8  fourth 8-bit data value (for &lt;D8&gt;)
     * @return LocoNet message for the requested message
     * @throws IllegalArgumentException if command is not a valid SV Programming Format 1 &lt;SV_CMD&gt; value
     */
    public static LocoNetMessage createSv1Message (
            int source,
            int destination,
            int subAddress,
            int command,
            int svNum,
            int version,
            int newVal,
            int d6,
            int d7,
            int d8)
            throws IllegalArgumentException {

        if (! isSupportedSv1Command(command)) {
            throw new IllegalArgumentException("Command is not a supported SV1 command"); // NOI18N
        }
        int[] contents = {command, svNum, version, newVal, subAddress, d6, d7, d8};
        log.debug("createSv1Message src={} dst={} subAddr={} data[]={}", source, destination, subAddress, contents);
        return LocoNetMessage.makePeerXfr(
                source,
                destination,
                contents,
                LNSV1_PEER_CODE_SV_VER1
        );
    }

    /**
     * Create LocoNet message for a query of an SV of this object.
     *
     * @param dst  address of the device to read from
     * @param subAddress  subaddress of the device to read from
     * @param svNum  SV number to read
     * @return LocoNet message
     */
public static LocoNetMessage createSv1ReadRequest(int dst, int subAddress, int svNum) {
    int dstExtr = dst | 0x0100; // force version 1 tag, cf. LnOpsModeProgrammer
    log.debug("createSv1ReadRequest dst={} dstExtr={} subAddr={}", dst, dstExtr, subAddress);
        return createSv1Message(LNSV1_LOCOBUFFER_ADDRESS, dstExtr, subAddress,
                Sv1Command.SV1_READ.sv_cmd, svNum, 0,0, 0, 0, 0);
    }

    /**
     * Simulate a read/probe reply for testing/developing.
     *
     * @param src board low address
     * @param dst dest high address, usually 0x50 for LocoBuffer/PC
     * @param subAddress board high address
     * @param version fictional firmware version number to add
     * @param svNum SV read
     * @return LocoNet message containing the reply
     */
    public static LocoNetMessage createSv1ReadReply(int src, int dst, int subAddress, int version, int svNum, int returnValue) {
        log.debug("createSv0ReadReply");
        int dstExtr = dst | 0x0100; // force version 1 tag, cf. LnOpsModeProgrammer
        return createSv0Message(src, dstExtr, subAddress,
                Sv1Command.SV1_READ.sv_cmd,
                svNum, version, 0, returnValue, 0, 0);
    }

    public static LocoNetMessage createSv1WriteRequest(int dst, int subAddress, int svNum, int newValue) {
        log.debug("createSv1WriteRequest");
        int dstExtr = dst | 0x0100; // force version 1 tag, cf. LnOpsModeProgrammer
        return createSv1Message(LNSV1_LOCOBUFFER_ADDRESS, dstExtr, subAddress,
                Sv1Command.SV1_WRITE.sv_cmd, svNum, 0, newValue, 0, 0, 0);
    }

    /**
     * Simulate a read/probe reply for testing/developing.
     *
     * @param src board low address
     * @param subAddress board high address
     * @param dst dest high address, usually 0x1050 for LocoBuffer/PC
     * @param version fictional firmware version number to add
     * @param svNum SV read
     * @return LocoNet message containing the reply
     */
    public static LocoNetMessage createSv1WriteReply(int src, int dst, int subAddress, int version, int svNum, int returnValue) {
        log.debug("createSv0WriteReply");
        int dstExtr = dst | 0x0100; // force version 1 tag, cf. LnOpsModeProgrammer
        return createSv0Message(src, dstExtr, subAddress,
                Sv1Command.SV1_WRITE.sv_cmd,
                svNum, version, 0, 0, 0, returnValue);
    }

    /**
     * Compose a message that changes the hardware board address of ALL connected
     * LNSV1 (LocoIO) boards.
     *
     * @param address the new base address of the LocoIO board to change
     * @param subAddress the new subAddress of the board
     * @return an array containing one or two LocoNet messages
     */
    public static LocoNetMessage[] createBroadcastSetAddress(int address, int subAddress) {
        LocoNetMessage[] messages = new LocoNetMessage[2];
        messages[0] = createSv1WriteRequest(LNSV1_BROADCAST_ADDRESS, 0, 1, address & 0xFF);
        if (subAddress != 0) {
            messages[1] = createSv1WriteRequest(LNSV1_BROADCAST_ADDRESS, 0, 2, subAddress);
        }
        return messages;
    }

    /**
     * Create a message to probe all connected LocoIO (LNSV1) units on a given LocoNet connection.
     *
     * @return the complete LocoNet message
     */
    public static LocoNetMessage createBroadcastProbeAll() {
        return createSv1ReadRequest(LNSV1_BROADCAST_ADDRESS, 0, 2);
    }

    public enum Sv1Command {
        SV1_WRITE (0x01),
        SV1_READ (0x02);

        private final int sv_cmd;

        Sv1Command(int sv_cmd) {
            this.sv_cmd = sv_cmd;
        }

        int getCmd() {return sv_cmd;}

        public static int getCmd(Sv1Command mt) {
            return mt.getCmd();
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(Lnsv1MessageContents.class);

}

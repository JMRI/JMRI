package jmri.jmrix.loconet.lnsvf1;

import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.locoio.LocoIO;
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
 * Code LOCOIO_SV_READ _or_ LOCOIO_SV_WRITE ----
 * 0xE5 OPC_PEER_XFER
 * 0x10 Message length
 * SRCL 0x50                0x50 // low address of LocoBuffer
 * DSTL LocoIO address
 * DSTH LocoIO sub-address
 * PXCT1
 * D1 LOCOIO_SV_READ _or_   LOCOIO_SV_WRITE // Read/Write command
 * D2 SV number             SV number
 * D3 0x00                  0x00
 * D4 0x00                  Data byte to Write
 * PXCT2
 * D5 0x01                  0x01 // LocoBuffer fixed sub-address
 * D6 0x00                  0x00
 * D7 0x00                  0x00
 * D8 0x00                  0x00
 * CHK Checksum             Checksum
 * </code></pre>
 *
 * LocoIO to PC reply message (OPC_PEER_XFER)
 * <pre><code>
 * Code LOCOIO_SV_READ _or_ LOCOIO_SV_WRITE ----
 * 0xE5 OPC_PEER_XFER
 * 0x10 Message length
 * SRCL LocoIO low address
 * DSTL 0x50                0x50 // address of LocoBuffer
 * DSTH 0x01                0x01 // sub-address of LocoBuffer
 * PXCT1 MSB LocoIO version // High order bit of LocoIO version
 * D1 LNSV1_READ _or_       LNSV1_WRITE // Copy of original Command
 * D2 SV number requested   SV number
 * D3 LSBs LocoIO version   // Lower 7 bits of LocoIO version
 * D4 0x00                  0x00
 * PXCT2 MSB Requested Data // High order bit of requested data
 * D5 LocoIO Sub-address
 * D6 Requested Data        0x00
 * D7 Requested Data + 1    0x00
 * D8 Requested Data + 2    Written Data
 * CHK Checksum             Checksum
 * </code></pre>
 *
 * @author John Plocher 2006, 2007
 * @author B. Milhaupt Copyright (C) 2015
 * @author E. Broerse Copyright (C) 2025
 */
public class Lnsv1MessageContents {
    public static final int LNSV1_BROADCAST_ADDRESS = 0x0100; // LocoIO broadcast (addr_H = 1)
    public static final int LNSV1_LOCOBUFFER_ADDRESS = 0x1050; // LocoBuffer reserved address (addr_H = 1)
    public static final int LNSV1_PEER_CODE_SV_VER1 = 0x08;

    private final int src_l;
    private final int src_h;
    private final int sv_cmd;
    private final int dst_l;
    private final int dst_h;
    private final int sv_adr;
    private final int vrs;
    private final int d4;
    private final int d6;
    private final int d7;
    private final int d8;

    // Helper to calculate LocoIO Sensor address from returned data - also in LocoNetMessage ?
    public static int SENSOR_ADR(int a1, int a2) {
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f)) + 1;
    }

    // LocoNet "SV 1 format" helper definitions: indexes into the LocoNet message
//    public final static int SV1_LENGTH_ELEMENT_INDEX = 1;
    public final static int SV1_SV_SRC_L_ELEMENT_INDEX = 2;
    public final static int SV1_SV_DST_L_ELEMENT_INDEX = 3;
    public final static int SV1_SV_DST_H_ELEMENT_INDEX = 4;
    public final static int SV1_SVX1_ELEMENT_INDEX = 5;
    public final static int SV1_SV_CMD_ELEMENT_INDEX = 6;
    public final static int SV1_SV_ADR_ELEMENT_INDEX = 7;
    public final static int SV1_SV_VRS_ELEMENT_INDEX = 8;
    public final static int SV1_SVD4_ELEMENT_INDEX = 9;
    public final static int SV1_SVX2_ELEMENT_INDEX = 10;
    public final static int SV1_SV_SRC_H_ELEMENT_INDEX = 11;
    public final static int SV1_SVD6_ELEMENT_INDEX = 12;
    public final static int SV1_SVD7_ELEMENT_INDEX = 13;
    public final static int SV1_SVD8_ELEMENT_INDEX = 14;

    //  helpers for decoding SV format 1 messages (versus other OCP_PEER_XFER messages with length 0x10)
    public final static int SV1_SRC_L_ELEMENT_MASK = 0x7f;
    // public final static int SV1_DST_L_ELEMENT_MASK = 0x7f;
    public final static int SV1_SVX1_ELEMENT_VALIDITY_CHECK_MASK = 0x70;
    public final static int SV1_SVX1_ELEMENT_VALIDITY_CHECK_VALUE = 0x00;
    // no high bit for SV1_SV_CMD_ELEMENT
    public final static int SV1_SV_ADR_SVADRX7_CHECK_MASK = 0x02;
    public final static int SV1_SV_VRS_VRSX7_CHECK_MASK = 0x04;
    public final static int SV1_SV_D4_D4X7_CHECK_MASK = 0x08;
    public final static int SV1_SVX2_ELEMENT_VALIDITY_CHECK_MASK = 0x70;
    public final static int SV0_SVX2_ELEMENT_VALIDITY_CHECK_VALUE = 0x10;
    public final static int SV1_SVX2_ELEMENT_VALIDITY_CHECK_VALUE = 0x00;
    // no high bit for sub-address SV1_SV_SRC_H_ELEMENT
    public final static int SV1_SV_D6_D6X7_CHECK_MASK = 0x02;
    public final static int SV1_SV_D7_D7X7_CHECK_MASK = 0x04;
    public final static int SV1_SV_D8_D8X7_CHECK_MASK = 0x08;

    // helpers for decoding SV_CMD
    public final static int SV_CMD_WRITE_ONE = 0x01;
    public final static int SV_CMD_READ_ONE = 0x02;

    // LocoNet "SV 1 format" helper definitions: SV_CMD "reply" bit
    public final static int SV1_SV_CMD_REPLY_BIT_NUMBER = 0x6;
//    public final static int SV1_SV_CMD_REPLY_BIT_MASK = (2^SV1_SV_CMD_REPLY_BIT_NUMBER);

    /**
     * Create a new LnSV1MessageContents object from a LocoNet message.
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
        src_h = m.getElement(SV1_SV_SRC_H_ELEMENT_INDEX);
        dst_l = m.getElement(SV1_SV_DST_L_ELEMENT_INDEX);
        dst_h = m.getElement(SV1_SV_DST_H_ELEMENT_INDEX);
        int svx1 = m.getElement(SV1_SVX1_ELEMENT_INDEX);
        int svx2 = m.getElement(SV1_SVX2_ELEMENT_INDEX);
        sv_cmd = m.getElement(SV1_SV_CMD_ELEMENT_INDEX);

        sv_adr  = m.getElement(SV1_SV_ADR_ELEMENT_INDEX)
                + (((svx1 & SV1_SV_ADR_SVADRX7_CHECK_MASK) == SV1_SV_ADR_SVADRX7_CHECK_MASK)
                ? 0x80 : 0);

        vrs = m.getElement(SV1_SV_VRS_ELEMENT_INDEX)
                + (((svx1 & SV1_SV_VRS_VRSX7_CHECK_MASK) == SV1_SV_VRS_VRSX7_CHECK_MASK)
                ? 0x80 : 0);

        d4 = m.getElement(SV1_SVD4_ELEMENT_INDEX)
                + (((svx1 & SV1_SV_D4_D4X7_CHECK_MASK) == SV1_SV_D4_D4X7_CHECK_MASK)
                ? 0x80 : 0);

        d6 = m.getElement(SV1_SVD6_ELEMENT_INDEX)
                + (((svx2 & SV1_SV_D6_D6X7_CHECK_MASK) == SV1_SV_D6_D6X7_CHECK_MASK)
                ? 0x80 : 0);

        d7 = m.getElement(SV1_SVD7_ELEMENT_INDEX)
                + (((svx2 & SV1_SV_D7_D7X7_CHECK_MASK) == SV1_SV_D7_D7X7_CHECK_MASK)
                ? 0x80 : 0);
        d8 = m.getElement(SV1_SVD8_ELEMENT_INDEX)
                + (((svx2 & SV1_SV_D8_D8X7_CHECK_MASK) == SV1_SV_D8_D8X7_CHECK_MASK)
                ? 0x80 : 0);
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

        // length of OPC_PEER_XFER must be 0x10
        if (m.getElement(1) != 0x10) {
            log.debug ("cannot be SV1 message because not length 0x10");  // NOI18N
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
                    & SV1_SVX2_ELEMENT_VALIDITY_CHECK_MASK)
                    != SV1_SVX2_ELEMENT_VALIDITY_CHECK_VALUE) {
                log.debug ("cannot be SV1 message because SVX2 upper nibble wrong: {}",
                        m.getElement(SV1_SVX2_ELEMENT_INDEX) & SV1_SVX2_ELEMENT_VALIDITY_CHECK_MASK);  // NOI18N
                return false;
            }
        }

        // check the <SV_CMD> value
        if (isSupportedSv1Command(m.getElement(SV1_SV_CMD_ELEMENT_INDEX))) {
            log.debug("LocoNet message is a supported SV Format 1 message");
            return true;
        }
        log.debug("LocoNet message is not a supported SV Format 2 message");  // NOI18N
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
     * Interpret a LocoNet message to determine its SV Programming Format 1 &lt;SV_CMD&gt;.
     * If the message is not an SV Programming Format 1 message, returns null.
     *
     * @param m  LocoNet message containing SV Programming Format 1 message
     * @return Sv1Command found in the SV Programming Format 1 message or null if not found
     */
    public static Sv1Command extractMessageType(LocoNetMessage m) {
        if (isSupportedSv1Message(m)) {
            int msgCmd = m.getElement(SV1_SV_CMD_ELEMENT_INDEX);
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
     * Interpret a LocoNet message to determine its SV Programming Format 1 &lt;SV_CMD&gt;.
     * If the message is not an SV Programming Format 1 message, return null.
     *
     * @param m  LocoNet message containing SV Programming Format 1 version field
     * @return Version found in the SV Programming Format 1 message or -1 if not found
     */
    public static int extractMessageVersion(LocoNetMessage m) {
        if (isSupportedSv1Message(m)) {
            int msgVrs = m.getElement(SV1_SV_VRS_ELEMENT_INDEX)
                    + (((m.getElement(SV1_SVX1_ELEMENT_INDEX) & SV1_SV_VRS_VRSX7_CHECK_MASK) == SV1_SV_VRS_VRSX7_CHECK_MASK)
                    ? 0x80 : 0);
            log.debug("LocoNet LNSV1 message contains version {}", msgVrs);  // NOI18N
            return msgVrs;
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
     * Interpret the SV Programming Format 2 message into a human-readable string.
     *
     * @param locale  locale to use for the human-readable string
     * @return String containing a human-readable version of the SV Programming
     *      Format 1 message, in the language specified by the Locale, if the
     *      properties have been translated to that Locale, else in the default
     *      English language.
     */
    public String toString(Locale locale) {
        String returnString;
        log.debug("interpreting an SV1 message - sv_cmd is {}", sv_cmd);  // NOI18N

        switch (sv_cmd) {
            case (SV_CMD_WRITE_ONE):
                returnString = Bundle.getMessage(locale, "SV1_WRITE_ONE_INTERPRETED",
                        src_l,
                        src_h,
                        dst_l,
                        dst_h,
                        sv_adr,
                        d4,
                        vrs);
                break;

            case (SV_CMD_READ_ONE):
                returnString = Bundle.getMessage(locale, "SV1_READ_ONE_INTERPRETED",
                        src_l,
                        src_h,
                        dst_l,
                        dst_h,
                        sv_adr,
                        d6,
                        d7,
                        d8,
                        vrs);
                break;

            default:
                return Bundle.getMessage(locale, "SV1_UNDEFINED_MESSAGE") + "\n";
        }

        log.debug("interpreted: {}", returnString);  // NOI18N
        return returnString + "\n"; // NOI18N
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
        return (sv_cmd == SV_CMD_READ_ONE);
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

    public int getSrcH() {
        return src_h;
    }

    public int getDstL() {
        return dst_l;
    }

    public int getDstH() {
        return dst_h;
    }

    public int getDestAddr() {
        if ((dst_l != 0x0100) && (dst_h != 0x0)) {
            return dst_l + 256*dst_h;
        }
        return -1;
    }

    public int getSubAddr() {
        return src_h;
    }

    public int getCmd() {
        return sv_cmd;
    }

    public int getSvNum() {
        if ((sv_cmd == Sv1Command.SV1_READ_ONE.sv_cmd) ||
                (sv_cmd == Sv1Command.SV1_WRITE_ONE.sv_cmd)) {
            return sv_adr;
        }
        return -1;
    }

    public int getSvValue() {
        if (sv_cmd == Sv1Command.SV1_READ_REPLY.sv_cmd) {
            return d6;
        }
        if (sv_cmd == Sv1Command.SV1_WRITE_ONE.sv_cmd) {
            if (vrs > 0) {
                return d8; // WriteReply
            } else {
                return d4; // WriteRequest
            }
        }
        return -1;
    }

    public int getVersionNum() {
        if ((sv_cmd == Lnsv1MessageContents.Sv1Command.SV1_READ_ONE.sv_cmd) ||
                (sv_cmd == Lnsv1MessageContents.Sv1Command.SV1_WRITE_ONE.sv_cmd) ||
                (sv_cmd == Lnsv1MessageContents.Sv1Command.SV1_READ_REPLY.sv_cmd)) {
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
     * Create a LocoNet message containing an SV Programming Format 1 message.
     * See Programmer message code in {@link jmri.jmrix.loconet.LnOpsModeProgrammer} loadSV1MessageFormat
     * TODO update doc
     * @param source  source device address (for &lt;SRC_L&gt;)
     * @param destination = SV format 1 destination address (for &lt;DST_L&gt;)
     * @param subAddress = SV format 1 destination subaddress (for &lt;DST_H&gt;)
     * @param command  SV Programming Format 1 command number (for &lt;SV_CMD&gt;)
     * @param svNum  SV Programming Format 1 7-bit SV number
     * @param data (d4)  SV first data value to write (for &lt;D4&gt;)
     * @param version  Programming Format 1 7-bit firmware version number, 0 in request, >0 in replies
     * @param d6  second data value (for &lt;D6&gt;)
     * @param d7  third data value (for &lt;D7&gt;)
     * @param d8  fourth data value (for &lt;D8&gt;)
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
            int data,
            int d6,
            int d7,
            int d8)
            throws IllegalArgumentException {

        if ( ! isSupportedSv1Command(command)) {
            throw new IllegalArgumentException("Command is not a supported SV1 command"); // NOI18N
        }
        int[] contents = {command, svNum, version, data, subAddress, d6, d7, d8};
        log.debug("createSv1Message0 src={} dst={} subAddr={} cata[]={}", source, destination, subAddress, contents);
        return LocoNetMessage.makePeerXfr(
                source,
                destination,
                contents,
                LNSV1_PEER_CODE_SV_VER1
        );
    }

//    public static LocoNetMessage readSV(int locoIOAddress, int locoIOSubAddress, int sv) {
//        log.debug("readSV {} from {}/{}", sv, locoIOAddress, locoIOSubAddress);
//        int[] contents = {Sv1Command.SV1_READ_ONE.sv_cmd, sv, 0, 0, locoIOSubAddress, 0, 0, 0};
//
//        return LocoNetMessage.makePeerXfr(
//                LNSV1_LOCOBUFFER_ADDRESS, // B'cast LocoBuffer address
//                locoIOAddress,
//                contents, // contains SV and SubAddr to read
//                LNSV1_PEER_CODE_SV_VER1
//        );
//    }

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
    log.debug("createSv1ReadRequest2 dst={} dstExtr={} subAddr={}", dst, dstExtr, subAddress);
        return createSv1Message(LNSV1_LOCOBUFFER_ADDRESS, dstExtr, subAddress,
                Sv1Command.SV1_READ_ONE.sv_cmd, svNum, 0,0, 0, 0, 0);
        // TODO verify against LocoIO.read(): BUG elem(4) is 00, not 01!
    }

    /**
     * Simulate a read/probe reply for testing/developing.
     *
     * @param src board low address
     * @param dst dest high address, usually 0x0150 for LocoBuffer/PC
     * @param subAddress board high address
     * @param version fictional firmware version number to add
     * @param svNum SV read
     * @return LocoNet message containing the reply
     */
    public static LocoNetMessage createSv1ReadReply(int src, int dst, int subAddress, int version, int svNum, int value) {
        log.debug("createSv1ReadReply0");
        int srcExtr = src | 0x0100; // dst or src? EBR
        return createSv1Message(srcExtr, dst, subAddress,
                Sv1Command.SV1_READ_ONE.sv_cmd,
                svNum, version, 0, value, 0, 0);
    }

    /**
     * Simulate a read reply to a given LocoNet message for testing/developing.
     *
     * @param m  the preceding LocoNet message
     * @param svValues  array containing the returned values for SV, SV+1 and SV+2.
     * @return  LocoNet message containing the reply, or null if preceding
     *          message isn't a query
     */
    public static LocoNetMessage createSv1ReadReply(LocoNetMessage m, int[] svValues, int version) {
        log.debug("createSv1ReadReply1");
        if (!isSupportedSv1Message(m)) {
            return null;
        }
        if (m.getElement(SV1_SV_CMD_ELEMENT_INDEX) != Sv1Command.SV1_READ_ONE.sv_cmd) {
            return null;
        }
        // first copy DST to SRC
        if (m.getElement(SV1_SV_DST_L_ELEMENT_INDEX) != LNSV1_BROADCAST_ADDRESS) {
            m.setElement(SV1_SV_SRC_L_ELEMENT_INDEX, m.getElement(SV1_SV_DST_L_ELEMENT_INDEX));
            m.setElement(SV1_SV_SRC_H_ELEMENT_INDEX, m.getElement(SV1_SV_DST_H_ELEMENT_INDEX));
        } else {
            m.setElement(SV1_SV_SRC_L_ELEMENT_INDEX, 57);
            m.setElement(SV1_SV_SRC_H_ELEMENT_INDEX, 1);
        }
        // then fill in LocoBuffer as DST
        m.setElement(SV1_SV_DST_L_ELEMENT_INDEX, LNSV1_LOCOBUFFER_ADDRESS & 0x7F);
        m.setElement(SV1_SV_DST_H_ELEMENT_INDEX, (LNSV1_LOCOBUFFER_ADDRESS >> 8) & 0x7F);

        m.setElement(SV1_SV_VRS_ELEMENT_INDEX, version & 0x7F);
        // TODO store hi bit of vrs in X1 bit 2
        m.setElement(SV1_SVD6_ELEMENT_INDEX, svValues[0] & 0x7F);
        m.setElement(SV1_SVD7_ELEMENT_INDEX, svValues[1] & 0x7F);
        m.setElement(SV1_SVD8_ELEMENT_INDEX, svValues[2] & 0x7F);
        // TODO store hi bits of svValues[0-2] in X2 bits 1-3

        return m;
    }

//    public static LocoNetMessage writeSV(int locoIOAddress, int locoIOSubAddress, int sv, int data) {
//        int[] contents = {Sv1Command.SV1_WRITE_ONE.sv_cmd, sv, 0, data, locoIOSubAddress, 0, 0, 0};
//        log.debug("writeSV");
//        return LocoNetMessage.makePeerXfr(
//                LNSV1_LOCOBUFFER_ADDRESS, // B'cast LocoBuffer address
//                locoIOAddress,
//                contents, // contains SV and SubAddr to read
//                LNSV1_PEER_CODE_SV_VER1
//        );
//    }

    public static LocoNetMessage createSv1WriteRequest(int dst, int subAddress, int svNum, int value) {
        log.debug("createSv1WriteRequest1");
        int dstExtr = dst | 0x0100; // force version 1 tag, cf. LnOpsModeProgrammer
        return createSv1Message(LNSV1_LOCOBUFFER_ADDRESS, dstExtr, subAddress,
                Sv1Command.SV1_WRITE_ONE.sv_cmd, svNum, 0, value, 0, 0, 0);
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
    public static LocoNetMessage createSv1WriteReply(int src, int dst, int subAddress, int version, int svNum, int value) {
        log.debug("createSv1WriteReply0");
        int srcExtr = src & 0xFF + 0x1000;
        return createSv1Message(srcExtr, dst, subAddress,
                Sv1Command.SV1_WRITE_ONE.sv_cmd,
                svNum, version, 0, 0, 0, value);
    }

    /**
     * Simulate a write reply from LocoNet for testing/developing.
     *
     * @param m  the preceding LocoNet message
     * @param version the device firmware version
     * @return  LocoNet message containing the reply, or null if preceding
     *          message isn't a WRITE_ONE request
     */
    public static LocoNetMessage createSv1WriteReply(LocoNetMessage m, int version) {
        log.debug("createSv1WriteReply1");
        if (!isSupportedSv1Message(m)) {
            return null;
        }
        if (m.getElement(SV1_SV_CMD_ELEMENT_INDEX) != Sv1Command.SV1_WRITE_ONE.sv_cmd) {
            return null;
        }
        int srcL = m.getElement(SV1_SV_SRC_L_ELEMENT_INDEX);
        int srcH = m.getElement(SV1_SV_SRC_H_ELEMENT_INDEX);
        int sv_val = m.getElement(SV1_SVD4_ELEMENT_INDEX);

        m.setElement(SV1_SV_DST_L_ELEMENT_INDEX, LNSV1_LOCOBUFFER_ADDRESS);
        m.setElement(SV1_SV_DST_H_ELEMENT_INDEX, 0x01);
        m.setElement(SV1_SRC_L_ELEMENT_MASK, srcL & 0x7F);
        m.setElement(SV1_SV_SRC_H_ELEMENT_INDEX, srcH & 0x7F);

        m.setElement(SV1_SV_VRS_ELEMENT_INDEX, version & 0x7F);
        // TODO store hi bit of vrs in X1 bit 2
        m.setElement(SV1_SVD8_ELEMENT_INDEX, sv_val & 0x7F);
        m.setElement(SV1_SVD4_ELEMENT_INDEX, 0x00);

        return m;
    }

    /**
     * Compose a message that changes the hardware board address of ALL connected
     * LNSV1 (LocoIO) boards.
     *
     * @param address the new base address of the LocoIO board to change
     * @param subAddress the new subAddress of the board
     * @return an array containing one or two LocoNet messages
     */
    public static LocoNetMessage[] createBroadcastAddress(int address, int subAddress) {
        log.debug("createBroadcastAddress");
        LocoNetMessage[] messages = new LocoNetMessage[2];
        messages[0] = LocoIO.writeSV(LNSV1_BROADCAST_ADDRESS, 0, 1, address & 0xFF);
        if (subAddress != 0) {
            messages[1] = LocoIO.writeSV(LNSV1_BROADCAST_ADDRESS, 0, 2, subAddress);
        }
        return messages;
    }

    /**
     * Create a message to probe all connected LocoIO (LNSV1) units on a given LocoNet connection.
     *
     * @return the complete LocoNet message
     */
    public static LocoNetMessage createBroadcastProbeAll() {
        return LocoIO.readSV(LNSV1_BROADCAST_ADDRESS, 0, 2);
    }

    public enum Sv1Command {
        SV1_WRITE_ONE (0x01),
        SV1_READ_ONE (0x02),
        SV1_WRITE_REPLY (0x01),
        SV1_READ_REPLY (0x02);

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

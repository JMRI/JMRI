package jmri.jmrix.nce;

import static jmri.jmrix.nce.NceBinaryCommand.*;
import static jmri.jmrix.nce.NceMessage.*;

import java.util.Arrays;
import jmri.NmraPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods to create standard NCE messages.
 * <p>
 * These methods used to be included in {@link jmri.jmrix.nce.NceMessage}, but
 * that had interesting, and not well understood, side effects in some test
 * environments that caused an intermittent inability to initialize the
 * NceMessage class.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2007
 * @author kcameron Copyright (C) 2014
 * @see jmri.jmrix.nce.NceMessage
 */
public class NceMessageUtil {

    // static methods to return a formatted message
    public static NceMessage getEnableMain(NceTrafficController tc) {
        // this command isn't supported by the NCE USB
        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
            log.error("attempt to send unsupported binary command ENABLE_MAIN_CMD to NCE USB");
            return null;
        }
        NceMessage m = new NceMessage(1);
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_1999) {
            m.setBinary(true);
            m.setReplyLen(1);
            m.setOpCode(ENABLE_MAIN_CMD);
        } else {
            m.setBinary(false);
            m.setOpCode('E');
        }
        return m;
    }

    public static NceMessage getKillMain(NceTrafficController tc) {
        // this command isn't supported by the NCE USB
        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
            log.error("attempt to send unsupported binary command KILL_MAIN_CMD to NCE USB");
            return null;
        }
        NceMessage m = new NceMessage(1);
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_1999) {
            m.setBinary(true);
            m.setReplyLen(1);
            m.setOpCode(KILL_MAIN_CMD);
        } else {
            m.setBinary(false);
            m.setOpCode('K');
        }
        return m;
    }

    /**
     * Enter programming track mode. Not supported on all NCE device types.
     *
     * @param tc the traffic controller with NCE connection details
     * @return a new message or null if sending this message may cause issues
     */
    public static NceMessage getProgMode(NceTrafficController tc) {
        // test if supported on current connection
        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE
                && (tc.getCmdGroups() & NceTrafficController.CMDS_PROGTRACK) != NceTrafficController.CMDS_PROGTRACK) {
            log.error("attempt to send unsupported binary command ENTER_PROG_CMD to NCE USB");
//			return null;
        }
        NceMessage m = new NceMessage(1);
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            tc.setNceProgMode(true);
            m.setBinary(true);
            m.setReplyLen(1);
            m.setOpCode(ENTER_PROG_CMD);
            m.setTimeout(SHORT_TIMEOUT);
        } else {
            m.setBinary(false);
            m.setOpCode('M');
            m.setTimeout(SHORT_TIMEOUT);
        }
        return m;
    }

    /**
     * Apparently the binary "exitProgrammingMode" command can crash the command
     * station if the EPROM was built before 2006. This method uses a state flag
     * ({@link NceTrafficController#getNceProgMode}) to detect whether a command
     * to enter program mode has been generated, and presumably sent, when using
     * the later EPROMS. Not supported on all NCE device types.
     *
     * @param tc the traffic controller with NCE connection details
     * @return a new message or null if sending this message may cause issues
     */
    public static NceMessage getExitProgMode(NceTrafficController tc) {
        NceMessage m = new NceMessage(1);
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            // Sending exit programming mode binary can crash pre 2006 EPROMs
            // assumption is that program mode hasn't been entered, so exit without 
            // sending command
            if (tc.getNceProgMode() == false) {
                return null;
            }
            // not supported by USB connected to SB3 or PH
            if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3
                    || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB5
                    || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_TWIN
                    || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERHOUSE) {
                log.error("attempt to send unsupported binary command EXIT_PROG_CMD to NCE USB");
//    			return null;
            }
            tc.setNceProgMode(false);
            m.setBinary(true);
            m.setReplyLen(1);
            m.setOpCode(EXIT_PROG_CMD);
            m.setTimeout(SHORT_TIMEOUT);
        } else {
            m.setBinary(false);
            m.setOpCode('X');
            m.setTimeout(SHORT_TIMEOUT);
        }
        return m;
    }

    /**
     * Read Paged mode CV on programming track. Not supported on all NCE device
     * types.
     *
     * @param tc the traffic controller with NCE connection details
     * @param cv the CV to read
     * @return a new message or null if sending this message may cause issues
     */
    public static NceMessage getReadPagedCV(NceTrafficController tc, int cv) {
        // test if supported on current connection
        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE
                && (tc.getCmdGroups() & NceTrafficController.CMDS_PROGTRACK) != NceTrafficController.CMDS_PROGTRACK) {
            log.error("attempt to send unsupported binary command READ_PAGED_CV_CMD to NCE USB");
//			return null;
        }
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            NceMessage m = new NceMessage(3);
            m.setBinary(true);
            m.setReplyLen(2);
            m.setOpCode(READ_PAGED_CV_CMD);
            m.setElement(1, (cv >> 8));
            m.setElement(2, (cv & 0x0FF));
            m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
            m.setTimeout(NCE_PAGED_CV_TIMEOUT);
            return m;
        } else {
            NceMessage m = new NceMessage(4);
            m.setBinary(false);
            m.setOpCode('R');
            m.addIntAsThree(cv, 1);
            m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
            m.setTimeout(NCE_PAGED_CV_TIMEOUT);
            return m;
        }
    }

    /**
     * Write paged mode CV to programming track. Not supported on all NCE device
     * types.
     *
     * @param tc  the traffic controller with NCE connection details
     * @param cv  the CV to write
     * @param val the value to write
     * @return a new message or null if sending this message may cause issues
     */
    public static NceMessage getWritePagedCV(NceTrafficController tc, int cv, int val) {
        // test if supported on current connection
        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE
                && (tc.getCmdGroups() & NceTrafficController.CMDS_PROGTRACK) != NceTrafficController.CMDS_PROGTRACK) {
            log.error("attempt to send unsupported binary command WRITE_PAGED_CV_CMD to NCE USB");
//			return null;
        }
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            NceMessage m = new NceMessage(4);
            m.setBinary(true);
            m.setReplyLen(1);
            m.setOpCode(WRITE_PAGED_CV_CMD);
            m.setElement(1, cv >> 8);
            m.setElement(2, cv & 0xFF);
            m.setElement(3, val);
            m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
            m.setTimeout(NCE_PAGED_CV_TIMEOUT);
            return m;
        } else {
            NceMessage m = new NceMessage(8);
            m.setBinary(false);
            m.setOpCode('P');
            m.addIntAsThree(cv, 1);
            m.setElement(4, ' ');
            m.addIntAsThree(val, 5);
            m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
            m.setTimeout(NCE_PAGED_CV_TIMEOUT);
            return m;
        }
    }

    public static NceMessage getReadRegister(NceTrafficController tc, int reg) {
        // not supported by USB connected to SB3 or PH
        if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3
                || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB5
                || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_TWIN
                || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERHOUSE) {
            log.error("attempt to send unsupported binary command READ_REG_CMD to NCE USB");
            return null;
        }
        if (reg > 8) {
            log.error("register number too large: " + reg);
        }
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            NceMessage m = new NceMessage(2);
            m.setBinary(true);
            m.setReplyLen(2);
            m.setOpCode(READ_REG_CMD);
            m.setElement(1, reg);
            m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
            m.setTimeout(NCE_PAGED_CV_TIMEOUT);
            return m;
        } else {
            NceMessage m = new NceMessage(2);
            m.setBinary(false);
            m.setOpCode('V');
            String s = "" + reg;
            m.setElement(1, s.charAt(s.length() - 1));
            m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
            m.setTimeout(NCE_PAGED_CV_TIMEOUT);
            return m;
        }
    }

    public static NceMessage getWriteRegister(NceTrafficController tc, int reg, int val) {
        // not supported by USB connected to SB3 or PH
        if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3
                || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB5
                || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_TWIN
                || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERHOUSE) {
            log.error("attempt to send unsupported binary command WRITE_REG_CMD to NCE USB");
            return null;
        }
        if (reg > 8) {
            log.error("register number too large: " + reg);
        }
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            NceMessage m = new NceMessage(3);
            m.setBinary(true);
            m.setReplyLen(1);
            m.setOpCode(WRITE_REG_CMD);
            m.setElement(1, reg);
            m.setElement(2, val);
            m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
            m.setTimeout(NCE_PAGED_CV_TIMEOUT);
            return m;
        } else {
            NceMessage m = new NceMessage(6);
            m.setBinary(false);
            m.setOpCode('S');
            String s = "" + reg;
            m.setElement(1, s.charAt(s.length() - 1));
            m.setElement(2, ' ');
            m.addIntAsThree(val, 3);
            m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
            m.setTimeout(NCE_PAGED_CV_TIMEOUT);
            return m;
        }
    }

    public static NceMessage getReadDirectCV(NceTrafficController tc, int cv) {
        // not supported by USB connected to SB3 or PH
        if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3
                || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB5
                || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_TWIN
                || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERHOUSE) {
            log.error("attempt to send unsupported binary command READ_DIR_CV_CMD to NCE USB");
            return null;
        }
        if (tc.getCommandOptions() < NceTrafficController.OPTION_2006) {
            log.error("getReadDirectCV with option " + tc.getCommandOptions());
            return null;
        }
        NceMessage m = new NceMessage(3);
        m.setBinary(true);
        m.setReplyLen(2);
        m.setOpCode(READ_DIR_CV_CMD);
        m.setElement(1, (cv >> 8));
        m.setElement(2, (cv & 0x0FF));
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(NCE_DIRECT_CV_TIMEOUT);
        return m;
    }

    public static NceMessage getWriteDirectCV(NceTrafficController tc, int cv, int val) {
        // not supported by USB connected to SB3 or PH
        if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3
                || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB5
                || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_TWIN
                || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERHOUSE) {
            log.error("attempt to send unsupported binary command WRITE_DIR_CV_CMD to NCE USB");
            return null;
        }
        if (tc.getCommandOptions() < NceTrafficController.OPTION_2006) {
            log.error("getWriteDirectCV with option " + tc.getCommandOptions());
        }
        NceMessage m = new NceMessage(4);
        m.setBinary(true);
        m.setReplyLen(1);
        m.setOpCode(WRITE_DIR_CV_CMD);
        m.setElement(1, cv >> 8);
        m.setElement(2, cv & 0xFF);
        m.setElement(3, val);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(NCE_DIRECT_CV_TIMEOUT);
        return m;
    }

    public static NceMessage sendPacketMessage(NceTrafficController tc, byte[] bytes) {
        return sendPacketMessage(tc, bytes, 2);
    }

    public static NceMessage sendPacketMessage(NceTrafficController tc, byte[] bytes, int retries) {
        // this command isn't supported by the NCE USB
        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
            log.error("attempt to send unsupported sendPacketMessage to NCE USB cmd: 0x" + Integer.toHexString(SENDn_BYTES_CMD + bytes.length));
            return null;
        }
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_1999) {
            if (bytes.length < 3 || bytes.length > 6) {
                log.error("Send of NCE track packet too short or long:" + Integer.toString(bytes.length)
                        + " packet:" + Arrays.toString(bytes));
            }
            NceMessage m = new NceMessage(2 + bytes.length);
            m.setBinary(true);
            m.setTimeout(SHORT_TIMEOUT);
            m.setReplyLen(1);
            int i = 0; // counter to make it easier to format the message

            m.setElement(i++, SENDn_BYTES_CMD + bytes.length);
            m.setElement(i++, retries);        // send this many retries. 
            for (int j = 0; j < bytes.length; j++) {
                m.setElement(i++, bytes[j] & 0xFF);
            }
            return m;
        } else {
            NceMessage m = new NceMessage(5 + 3 * bytes.length);
            m.setBinary(false);
            int i = 0; // counter to make it easier to format the message

            m.setElement(i++, 'S');  // "S C02 " means sent it twice
            m.setElement(i++, ' ');
            m.setElement(i++, 'C');
            m.setElement(i++, '0');
            m.setElement(i++, '2');

            for (int j = 0; j < bytes.length; j++) {
                m.setElement(i++, ' ');
                m.addIntAsTwoHex(bytes[j] & 0xFF, i);
                i = i + 2;
            }
            m.setTimeout(SHORT_TIMEOUT);
            return m;
        }
    }

    public static NceMessage createBinaryMessage(NceTrafficController tc, byte[] bytes) {
        if (tc.getCommandOptions() < NceTrafficController.OPTION_2004) {
            log.error("Attempt to send NCE command to EPROM built before 2004");
        }
        if (bytes.length < 1 || bytes.length > 20) {
            log.error("NCE command message length error:" + bytes.length);
        }
        NceMessage m = new NceMessage(bytes.length);
        m.setBinary(true);
        m.setReplyLen(1);
        m.setTimeout(SHORT_TIMEOUT);
        for (int j = 0; j < bytes.length; j++) {
            m.setElement(j, bytes[j] & 0xFF);
        }
        return m;
    }

    public static NceMessage createBinaryMessage(NceTrafficController tc, byte[] bytes, int replyLen) {
        if (tc.getCommandOptions() < NceTrafficController.OPTION_2004) {
            log.error("Attempt to send NCE command to EPROM built before 2004");
        }
        if (bytes.length < 1 || bytes.length > 20) {
            log.error("NCE command message length error:" + bytes.length);
        }

        NceMessage m = new NceMessage(bytes.length);
        m.setBinary(true);
        m.setReplyLen(replyLen);
        m.setTimeout(SHORT_TIMEOUT);

        for (int j = 0; j < bytes.length; j++) {
            m.setElement(j, bytes[j] & 0xFF);
        }
        return m;
    }

    public static NceMessage queuePacketMessage(NceTrafficController tc, byte[] bytes) {
        // this command isn't supported by the NCE USB
        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
            log.error("attempt to send unsupported queuePacketMessage to NCE USB");
            return null;
        }
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_1999) {
            if (bytes.length < 3 || bytes.length > 6) {
                log.error("Queue of NCE track packet too long:" + Integer.toString(bytes.length)
                        + " packet :" + Arrays.toString(bytes));
            }
            NceMessage m = new NceMessage(1 + bytes.length);
            m.setBinary(true);
            m.setReplyLen(1);
            int i = 0; // counter to make it easier to format the message

            m.setElement(i++, QUEUEn_BYTES_CMD + bytes.length);
            for (int j = 0; j < bytes.length; j++) {
                m.setElement(i++, bytes[j] & 0xFF);
            }
            return m;
        } else {
            NceMessage m = new NceMessage(1 + 3 * bytes.length);
            m.setBinary(false);
            int i = 0; // counter to make it easier to format the message

            m.setElement(i++, 'Q');  // "S C02 " means sent it twice

            for (int j = 0; j < bytes.length; j++) {
                m.setElement(i++, ' ');
                m.addIntAsTwoHex(bytes[j] & 0xFF, i);
                i = i + 2;
            }
            return m;
        }
    }

    public static NceMessage createAccySignalMacroMessage(NceTrafficController tc, int op, int addr, int data) {
        if (tc.getCommandOptions() < NceTrafficController.OPTION_2004) {
            log.error("Attempt to send NCE command to EPROM built before 2004");
        }
        NceMessage m = new NceMessage(5);
        m.setBinary(true);
        m.setReplyLen(1);
        m.setTimeout(SHORT_TIMEOUT);
        m.setOpCode(SEND_ACC_SIG_MACRO_CMD);
        m.setElement(1, (addr >> 8) & 0xFF);
        m.setElement(2, addr & 0xFF);
        m.setElement(3, op);
        m.setElement(4, data);
        return m;
    }

    public static NceMessage createAccDecoderPktOpsMode(NceTrafficController tc, int accyAddr, int cvAddr, int cvData) {
        NceMessage m = new NceMessage(6);
        m.setBinary(true);
        m.setReplyLen(1);
        m.setTimeout(SHORT_TIMEOUT);
        byte[] mess = usbOpsModeAccy(accyAddr, cvAddr, cvData);
        m.setOpCode(mess[0]);
        m.setElement(1, mess[1]);
        m.setElement(2, mess[2]);
        m.setElement(3, mess[3]);
        m.setElement(4, mess[4]);
        m.setElement(5, mess[5]);
        return m;
    }

    /**
     *
     * @param address the value of address
     * @return command as a byte sequence
     */
    public static byte[] accMemoryRead(int address) {
        int addr_h = address / 256;
        int addr_l = address & 255;
        byte[] retVal = new byte[3];
        retVal[0] = (byte) (READ16_CMD); // read 16 bytes command
        retVal[1] = (byte) (addr_h); // high address
        retVal[2] = (byte) (addr_l); // low address
        return retVal;
    }

    /**
     * Create a NCE USB compatible ops mode accessory message.
     *
     * @param accyAddr locomotive address
     * @param cvAddr   CV to set
     * @param cvData   value to set CV to
     * @return ops mode message
     */
    public static byte[] usbOpsModeAccy(int accyAddr, int cvAddr, int cvData) {
        byte[] retVal = new byte[6];
        int accyAddr_h = accyAddr / 256;
        int accyAddr_l = accyAddr & 255;
        int cvAddr_h = cvAddr / 256;
        int cvAddr_l = cvAddr & 255;
        retVal[0] = (byte) (OPS_PROG_ACCY_CMD); // NCE ops mode accy command
        retVal[1] = (byte) (accyAddr_h); // accy high address
        retVal[2] = (byte) (accyAddr_l); // accy low address
        retVal[3] = (byte) (cvAddr_h); // CV high address
        retVal[4] = (byte) (cvAddr_l); // CV low address
        retVal[5] = (byte) (cvData); // CV data
        return retVal;
    }

    /**
     *
     * @param cab the value of cab
     * @return command as a byte sequence
     */
    public static byte[] usbSetCabId(int cab) {
        byte[] retVal = new byte[2];
        retVal[0] = (byte) (USB_SET_CAB_CMD); // read N bytes command
        retVal[1] = (byte) (cab); // cab number
        return retVal;
    }

    /**
     *
     * @param address the value of address
     * @return command as a byte sequence
     */
    public static byte[] accMemoryWrite1(int address) {
        int addr_h = address / 256;
        int addr_l = address & 255;
        byte[] retVal = new byte[3 + 1];
        retVal[0] = (byte) (WRITE1_CMD); // write 4 bytes command
        retVal[1] = (byte) (addr_h); // high address
        retVal[2] = (byte) (addr_l); // low address
        return retVal;
    }

    /**
     * Create a NCE USB compatible ops mode loco message.
     *
     * @param tc       traffic controller; ignored
     * @param locoAddr locomotive address
     * @param cvAddr   CV to set
     * @param cvData   value to set CV to
     * @return ops mode message
     */
    public static byte[] usbOpsModeLoco(NceTrafficController tc, int locoAddr, int cvAddr, int cvData) {
        byte[] retVal = new byte[6];
        int locoAddr_h = locoAddr / 256;
        int locoAddr_l = locoAddr & 255;
        int cvAddr_h = cvAddr / 256;
        int cvAddr_l = cvAddr & 255;
        retVal[0] = (byte) (OPS_PROG_LOCO_CMD); // NCE ops mode loco command
        retVal[1] = (byte) (locoAddr_h); // loco high address
        retVal[2] = (byte) (locoAddr_l); // loco low address
        retVal[3] = (byte) (cvAddr_h); // CV high address
        retVal[4] = (byte) (cvAddr_l); // CV low address
        retVal[5] = (byte) (cvData); // CV data
        return retVal;
    }

    /**
     *
     * @return command as a byte sequence
     */
    public static byte[] accStartClock() {
        byte[] retVal = new byte[1];
        retVal[0] = (byte) (START_CLOCK_CMD); // start clock command
        return retVal;
    }

    /**
     *
     * @param data the value of data
     * @return command as a byte sequence
     */
    public static byte[] usbMemoryWrite1(byte data) {
        byte[] retVal = new byte[2];
        retVal[0] = (byte) (USB_MEM_WRITE_CMD); // write 2 bytes command
        retVal[1] = (data); // data
        return retVal;
    }

    /**
     *
     * @param hours   the value of hours
     * @param minutes the value of minutes
     * @return command as a byte sequence
     */
    public static byte[] accSetClock(int hours, int minutes) {
        byte[] retVal = new byte[3];
        retVal[0] = (byte) (SET_CLOCK_CMD); // set clock command
        retVal[1] = (byte) (hours); // hours
        retVal[2] = (byte) (minutes); // minutes
        return retVal;
    }

    /**
     *
     * @param address the value of address
     * @return command as a byte sequence
     */
    public static byte[] accMemoryWrite2(int address) {
        int addr_h = address / 256;
        int addr_l = address & 255;
        byte[] retVal = new byte[3 + 2];
        retVal[0] = (byte) (WRITE2_CMD); // write 4 bytes command
        retVal[1] = (byte) (addr_h); // high address
        retVal[2] = (byte) (addr_l); // low address
        return retVal;
    }

    /**
     *
     * @param flag the value of flag
     * @return command as a byte sequence
     */
    public static byte[] accSetClock1224(boolean flag) {
        int bit = 0;
        if (flag) {
            bit = 1;
        }
        byte[] retVal = new byte[2];
        retVal[0] = (byte) (CLOCK_1224_CMD); // set clock 12/24 command
        retVal[1] = (byte) (bit); // 12 - 0, 24 - 1
        return retVal;
    }

    /**
     *
     * @return command as a byte sequence
     */
    public static byte[] accStopClock() {
        byte[] retVal = new byte[1];
        retVal[0] = (byte) (STOP_CLOCK_CMD); // stop clock command
        return retVal;
    }

    /**
     *
     * @param cabId the value of cabId
     * @return command as a byte sequence
     */
    public static byte[] accAiu2Read(int cabId) {
        byte[] retVal = new byte[1 + 1];
        retVal[0] = (byte) (READ_AUI2_CMD); // write 4 bytes command
        retVal[1] = (byte) (cabId); // cab address
        return retVal;
    }

    /**
     * Read one byte from NCE command station memory
     *
     * @param address address to read from
     * @return binary command to read one byte
     */
    public static byte[] accMemoryRead1(int address) {
        int addr_h = address / 256;
        int addr_l = address & 255;
        byte[] retVal = new byte[3];
        retVal[0] = (byte) (READ1_CMD); // read 1 byte command
        retVal[1] = (byte) (addr_h); // high address
        retVal[2] = (byte) (addr_l); // low address
        return retVal;
    }

    /**
     *
     * @param cab the value of cab
     * @param loc the value of loc
     * @return command as a byte sequence
     */
    public static byte[] usbMemoryPointer(int cab, int loc) {
        byte[] retVal = new byte[3];
        retVal[0] = (byte) (USB_MEM_POINTER_CMD); // read N bytes command
        retVal[1] = (byte) (cab); // cab number
        retVal[2] = (byte) (loc); // memory offset
        return retVal;
    }

    /**
     *
     * @param locoAddr   the value of locoAddr
     * @param locoSubCmd the value of locoSubCmd
     * @param locoData   the value of locoData
     * @return command as a byte sequence
     */
    public static byte[] nceLocoCmd(int locoAddr, byte locoSubCmd, byte locoData) {
        if (locoSubCmd < 1 || locoSubCmd > 23) {
            log.error("invalid NCE loco command " + locoSubCmd);
            return null;
        }
        int locoAddr_h = locoAddr / 256;
        int locoAddr_l = locoAddr & 255;
        byte[] retVal = new byte[5];
        retVal[0] = (byte) (LOCO_CMD); // NCE Loco command
        retVal[1] = (byte) (locoAddr_h); // loco high address
        retVal[2] = (byte) (locoAddr_l); // loco low address
        retVal[3] = locoSubCmd; // sub command
        retVal[4] = locoData; // sub data
        return retVal;
    }

    /**
     *
     * @param num the value of num
     * @return command as a byte sequence
     */
    public static byte[] usbMemoryRead(int num) {
        byte[] retVal = new byte[2];
        retVal[0] = (byte) (USB_MEM_READ_CMD); // read N bytes command
        retVal[1] = (byte) (num); // byte count
        return retVal;
    }

    /**
     *
     * @param address the value of address
     * @param num     the value of num
     * @return command as a byte sequence
     */
    public static byte[] accMemoryWriteN(int address, int num) {
        int addr_h = address / 256;
        int addr_l = address & 255;
        byte[] retVal = new byte[4 + 16];
        retVal[0] = (byte) (WRITE_N_CMD); // write n bytes command
        retVal[1] = (byte) (addr_h); // high address
        retVal[2] = (byte) (addr_l); // low address
        retVal[3] = (byte) num; // number of bytes to write
        return retVal;
    }

    /**
     *
     * @param address the value of address
     * @return command as a byte sequence
     */
    public static byte[] accMemoryWrite8(int address) {
        int addr_h = address / 256;
        int addr_l = address & 255;
        byte[] retVal = new byte[3 + 8];
        retVal[0] = (byte) (WRITE8_CMD); // write 8 bytes command
        retVal[1] = (byte) (addr_h); // high address
        retVal[2] = (byte) (addr_l); // low address
        return retVal;
    }

    /**
     *
     * @param address the value of address
     * @return command as a byte sequence
     */
    public static byte[] accMemoryWrite4(int address) {
        int addr_h = address / 256;
        int addr_l = address & 255;
        byte[] retVal = new byte[3 + 4];
        retVal[0] = (byte) (WRITE4_CMD); // write 4 bytes command
        retVal[1] = (byte) (addr_h); // high address
        retVal[2] = (byte) (addr_l); // low address
        return retVal;
    }

    /**
     * Create NCE EPROM revision message. The reply format is:
     * {@literal VV.MM.mm}
     *
     * @return the revision message
     */
    public static byte[] getNceEpromRev() {
        byte[] retVal = new byte[1];
        retVal[0] = (byte) (SW_REV_CMD);
        return retVal;
    }

    /**
     *
     * @param number the value of number
     * @param closed the value of closed
     * @return command as a byte sequence
     */
    public static byte[] accDecoder(int number, boolean closed) {
        if (number < NmraPacket.accIdLowLimit || number > NmraPacket.accIdAltHighLimit) {
            log.error("invalid NCE accessory address " + number);
            return null;
        }
        /* Moved to NceMessageCheck
        // USB connected to PowerCab or SB3 can only access addresses up to 250
        if (number > 250
        && ((NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_POWERCAB) ||
        (NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_SB3))) {
        log.error("invalid NCE accessory address for USB " + number);
        return null;
        }
         */
        byte op_1;
        if (closed) {
            op_1 = 3;
        } else {
            op_1 = 4;
        }
        int addr_h = number / 256;
        int addr_l = number & 255;
        byte[] retVal = new byte[5];
        retVal[0] = (byte) (ACC_CMD); // NCE accessory command
        retVal[1] = (byte) (addr_h); // high address
        retVal[2] = (byte) (addr_l); // low address
        retVal[3] = op_1; // command
        retVal[4] = (byte) 0; // zero out last byte for acc
        return retVal;
    }

    /**
     *
     * @param ratio the value of ratio
     * @return command as a byte sequence
     */
    public static byte[] accSetClockRatio(int ratio) {
        byte[] retVal = new byte[2];
        retVal[0] = (byte) (CLOCK_RATIO_CMD); // set clock command
        retVal[1] = (byte) (ratio); // fast clock ratio
        return retVal;
    }

    private final static Logger log = LoggerFactory.getLogger(NceMessageUtil.class);
}

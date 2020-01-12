package jmri.jmrix.nce;

import java.util.Arrays;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes a message to an NCE command station.
 * <p>
 * The {@link NceReply} class handles the response from the command station.
 * <p>
 * The NCE protocol has "binary" and "ASCII" command sets. Depending on the
 * version of the EPROM it contains, NCE command stations have different support
 * for command sets:
 * <ul>
 * <li>1999 - All ASCII works. Binary works except for programming.
 * <li>2004 - ASCII needed for programming, binary for everything else.
 * <li>2006 - binary needed for everything
 * </ul>
 * See the {@link NceTrafficController#setCommandOptions(int)} method for more
 * information.
 * <p>
 * Apparently the binary "exitProgrammingMode" command can crash the command
 * station if the EPROM was built before 2006. This method uses a state flag
 * ({@link NceTrafficController#getNceProgMode}) to detect whether a command to
 * enter program mode has been generated, and presumably sent, when using the
 * later EPROMS.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2007
 * @author kcameron Copyright (C) 2014
 */
public class NceMessage extends jmri.jmrix.AbstractMRMessage {
 
    protected static final jmri.jmrix.nce.ncemon.NceMonBinary nceMon = new jmri.jmrix.nce.ncemon.NceMonBinary();

    public static final int NOP_CMD = 0x80; //NCE NOP command
    public static final int ASSIGN_CAB_CMD = 0x81; // NCE Assign loco to cab command, NCE-USB no
    public static final int READ_CLOCK_CMD = 0x82; // NCE read clock command, NCE-USB no
    public static final int STOP_CLOCK_CMD = 0x83; // NCE stop clock command, NCE-USB no
    public static final int START_CLOCK_CMD = 0x84; // NCE start clock command, NCE-USB no
    public static final int SET_CLOCK_CMD = 0x85; // NCE set clock command, NCE-USB no
    public static final int CLOCK_1224_CMD = 0x86; // NCE change clock 12/24 command, NCE-USB no
    public static final int CLOCK_RATIO_CMD = 0x87; // NCE set clock ratio command, NCE-USB no
    public static final int DEQUEUE_CMD = 0x88; // NCE dequeue packets based on loco addr, NCE-USB no

    public static final int READ_AUI4_CMD = 0x8A; // NCE read status of AUI yy, returns four bytes, NCE-USB no

    public static final int DUMMY_CMD = 0x8C; // NCE Dummy instruction, NCE-USB yes
    public static final int SPEED_MODE_CMD = 0x8D; // NCE set speed mode, NCE-USB no
    public static final int WRITE_N_CMD = 0x8E; // NCE write up to 16 bytes of memory command, NCE-USB no
    public static final int READ16_CMD = 0x8F; // NCE read 16 bytes of memory command, NCE-USB no
    public static final int DISPLAY3_CMD = 0x90; // NCE write 16 char to cab display line 3, NCE-USB no
    public static final int DISPLAY4_CMD = 0x91; // NCE write 16 char to cab display line 4, NCE-USB no
    public static final int DISPLAY2_CMD = 0x92; // NCE write 8 char to cab display line 2 right, NCE-USB no
    public static final int QUEUE3_TMP_CMD = 0x93; // NCE queue 3 bytes to temp queue, NCE-USB no
    public static final int QUEUE4_TMP_CMD = 0x94; // NCE queue 4 bytes to temp queue, NCE-USB no
    public static final int QUEUE5_TMP_CMD = 0x95; // NCE queue 5 bytes to temp queue, NCE-USB no
    public static final int QUEUE6_TMP_CMD = 0x96; // NCE queue 6 bytes to temp queue, NCE-USB no
    public static final int WRITE1_CMD = 0x97; // NCE write 1 bytes of memory command, NCE-USB no
    public static final int WRITE2_CMD = 0x98; // NCE write 2 bytes of memory command, NCE-USB no
    public static final int WRITE4_CMD = 0x99; // NCE write 4 bytes of memory command, NCE-USB no
    public static final int WRITE8_CMD = 0x9A; // NCE write 8 bytes of memory command, NCE-USB no
    public static final int READ_AUI2_CMD = 0x9B; // NCE read status of AUI yy, returns two bytes, NCE-USB >= 1.65
    public static final int MACRO_CMD = 0x9C; // NCE execute macro n, NCE-USB yes
    public static final int READ1_CMD = 0x9D; // NCE read 1 byte of memory command, NCE-USB no
    public static final int ENTER_PROG_CMD = 0x9E; //NCE enter programming track mode command
    public static final int EXIT_PROG_CMD = 0x9F; //NCE exit programming track mode command
    public static final int WRITE_PAGED_CV_CMD = 0xA0; //NCE write CV paged command
    public static final int READ_PAGED_CV_CMD = 0xA1; //NCE read CV paged command
    public static final int LOCO_CMD = 0xA2; // NCE loco control command, NCE-USB yes
    public static final int QUEUE3_TRK_CMD = 0xA3; // NCE queue 3 bytes to track queue, NCE-USB no
    public static final int QUEUE4_TRK_CMD = 0xA4; // NCE queue 4 bytes to track queue, NCE-USB no
    public static final int QUEUE5_TRK_CMD = 0xA5; // NCE queue 5 bytes to track queue, NCE-USB no
    public static final int WRITE_REG_CMD = 0xA6; //NCE write register command
    public static final int READ_REG_CMD = 0xA7; //NCE read register command
    public static final int WRITE_DIR_CV_CMD = 0xA8; //NCE write CV direct command
    public static final int READ_DIR_CV_CMD = 0xA9; //NCE read CV direct command
    public static final int SW_REV_CMD = 0xAA; // NCE get EPROM revision cmd, Reply Format: VV.MM.mm, NCE-USB yes
    public static final int RESET_SOFT_CMD = 0xAB; // NCE soft reset command, NCE-USB no
    public static final int RESET_HARD_CMD = 0xAC; // NCE hard reset command, NCE-USB no
    public static final int SEND_ACC_SIG_MACRO_CMD = 0xAD; // NCE send NMRA aspect command
    public static final int OPS_PROG_LOCO_CMD = 0xAE;   // NCE ops mode program loco, NCE-USB yes
    public static final int OPS_PROG_ACCY_CMD = 0xAF;   // NCE ops mode program accessories, NCE-USB yes
    public static final int FACTORY_TEST_CMD = 0xB0;    // NCE factory test, NCE-USB yes
    public static final int USB_SET_CAB_CMD = 0xB1;     // NCE set cab address in USB, NCE-USB yes
    public static final int USB_MEM_POINTER_CMD = 0xB3; // NCE set memory context pointer, NCE-USB >= 1.65
    public static final int USB_MEM_WRITE_CMD = 0xB4;   // NCE write memory, NCE-USB >= 1.65
    public static final int USB_MEM_READ_CMD = 0xB5;    // NCE read memory, NCE-USB >= 1.65

    // The following commands are not supported by the NCE USB  
    public static final int ENABLE_MAIN_CMD = 0x89; //NCE enable main track, kill programming command
    public static final int KILL_MAIN_CMD = 0x8B; //NCE kill main track, enable programming command
    public static final int SENDn_BYTES_CMD = 0x90; //NCE send 3 to 6 bytes (0x9n, n = 3-6) command
    public static final int QUEUEn_BYTES_CMD = 0xA0; //NCE queue 3 to 6 bytes (0xAn, n = 3-6) command

    // some constants
    protected static final int NCE_PAGED_CV_TIMEOUT = 20000;
    protected static final int NCE_DIRECT_CV_TIMEOUT = 10000;
    protected static final int SHORT_TIMEOUT = 10000; // worst case is when loading the first panel

    public static final int REPLY_1 = 1; // reply length of 1 byte
    public static final int REPLY_2 = 2; // reply length of 2 byte
    public static final int REPLY_4 = 4; // reply length of 4 byte
    public static final int REPLY_16 = 16; // reply length of 16 bytes 

    public NceMessage() {
        super();
    }

    // create a new one
    public NceMessage(int i) {
        super(i);
    }

    // copy one
    public NceMessage(@Nonnull NceMessage m) {
        super(m);
        replyLen = m.replyLen;
    }

    // from String
    public NceMessage(@Nonnull String m) {
        super(m);
    }

    // default to expecting one reply character
    int replyLen = 1;

    /**
     * Set the number of characters expected back from the command station. Used
     * in binary mode, where there's no end-of-reply string to look for.
     * 
     * @param len length of expected reply
     */
    public void setReplyLen(int len) {
        replyLen = len;
    }

    public int getReplyLen() {
        return replyLen;
    }

    // diagnose format
    public boolean isKillMain() {
        if (isBinary()) {
            return getOpCode() == KILL_MAIN_CMD;
        } else {
            return getOpCode() == 'K';
        }
    }

    public boolean isEnableMain() {
        if (isBinary()) {
            return getOpCode() == ENABLE_MAIN_CMD;
        } else {
            return getOpCode() == 'E';
        }
    }

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
            m.setReplyLen(REPLY_1);
            m.setOpCode(KILL_MAIN_CMD);
        } else {
            m.setBinary(false);
            m.setOpCode('K');
        }
        return m;
    }

    /**
     * enter programming track mode
     *
     * @param tc controller for the associated connection
     * @return a new message to enter programming track mode
     */
    @Nonnull
    public static NceMessage getProgMode(@Nonnull NceTrafficController tc) {
        // test if supported on current connection
        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE &&
                (tc.getCmdGroups() & NceTrafficController.CMDS_PROGTRACK) != NceTrafficController.CMDS_PROGTRACK) {
            log.error("attempt to send unsupported binary command ENTER_PROG_CMD to NCE USB");
            //   return null;
        }
        NceMessage m = new NceMessage(1);
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            tc.setNceProgMode(true);
            m.setBinary(true);
            m.setReplyLen(REPLY_1);
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
     * the later EPROMS.
     *
     * @param tc controller for the associated connection
     * @return a new message to exit programming track mode
     */
    @CheckForNull
    public static NceMessage getExitProgMode(@Nonnull NceTrafficController tc) {
        NceMessage m = new NceMessage(1);
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            // Sending exit programming mode binary can crash pre 2006 EPROMs
            // assumption is that program mode hasn't been entered, so exit without 
            // sending command
            if (tc.getNceProgMode() == false) {
                return null;
            }
            // not supported by USB connected to SB3 or PH
            if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3 ||
                    tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB5 ||
                    tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERPRO) {
                log.error("attempt to send unsupported binary command EXIT_PROG_CMD to NCE USB");
                //       return null;
            }
            tc.setNceProgMode(false);
            m.setBinary(true);
            m.setReplyLen(REPLY_1);
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
     * Read Paged mode CV on programming track.
     *
     * @param tc controller for the associated connection
     * @param cv the CV to read
     * @return a new message to read a CV
     */
    @Nonnull
    public static NceMessage getReadPagedCV(@Nonnull NceTrafficController tc, int cv) {
        // test if supported on current connection
        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE &&
                (tc.getCmdGroups() & NceTrafficController.CMDS_PROGTRACK) != NceTrafficController.CMDS_PROGTRACK) {
            log.error("attempt to send unsupported binary command READ_PAGED_CV_CMD to NCE USB");
            //   return null;
        }
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            NceMessage m = new NceMessage(3);
            m.setBinary(true);
            m.setReplyLen(REPLY_2);
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
     * Write paged mode CV to programming track.
     *
     * @param tc controller for the associated connection
     * @param cv CV to write
     * @param val value to write to cv
     * @return a new message to write a CV
     */
    @Nonnull
    public static NceMessage getWritePagedCV(@Nonnull NceTrafficController tc, int cv, int val) {
        // test if supported on current connection
        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE &&
                (tc.getCmdGroups() & NceTrafficController.CMDS_PROGTRACK) != NceTrafficController.CMDS_PROGTRACK) {
            log.error("attempt to send unsupported binary command WRITE_PAGED_CV_CMD to NCE USB");
            //   return null;
        }
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            NceMessage m = new NceMessage(4);
            m.setBinary(true);
            m.setReplyLen(REPLY_1);
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

    @CheckForNull
    public static NceMessage getReadRegister(@Nonnull NceTrafficController tc, int reg) {
        // not supported by USB connected to SB3 or PH
        if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3 ||
                tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB5 ||
                tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERPRO) {
            log.error("attempt to send unsupported binary command READ_REG_CMD to NCE USB");
            return null;
        }
        if (reg > 8) {
            log.error("register number too large: " + reg);
        }
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            NceMessage m = new NceMessage(2);
            m.setBinary(true);
            m.setReplyLen(REPLY_2);
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
        if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3 ||
                tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB5 ||
                tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERPRO) {
            log.error("attempt to send unsupported binary command WRITE_REG_CMD to NCE USB");
            return null;
        }
        if (reg > 8) {
            log.error("register number too large: " + reg);
        }
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            NceMessage m = new NceMessage(3);
            m.setBinary(true);
            m.setReplyLen(REPLY_1);
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
        if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3 ||
                tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB5 ||
                tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERPRO) {
            log.error("attempt to send unsupported binary command READ_DIR_CV_CMD to NCE USB");
            return null;
        }
        if (tc.getCommandOptions() < NceTrafficController.OPTION_2006) {
            log.error("getReadDirectCV with option " + tc.getCommandOptions());
            return null;
        }
        NceMessage m = new NceMessage(3);
        m.setBinary(true);
        m.setReplyLen(REPLY_2);
        m.setOpCode(READ_DIR_CV_CMD);
        m.setElement(1, (cv >> 8));
        m.setElement(2, (cv & 0x0FF));
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(NCE_DIRECT_CV_TIMEOUT);
        return m;
    }

    public static NceMessage getWriteDirectCV(NceTrafficController tc, int cv, int val) {
        // not supported by USB connected to SB3 or PH
        if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3 ||
                tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB5 ||
                tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERPRO) {
            log.error("attempt to send unsupported binary command WRITE_DIR_CV_CMD to NCE USB");
            return null;
        }
        if (tc.getCommandOptions() < NceTrafficController.OPTION_2006) {
            log.error("getWriteDirectCV with option " + tc.getCommandOptions());
        }
        NceMessage m = new NceMessage(4);
        m.setBinary(true);
        m.setReplyLen(REPLY_1);
        m.setOpCode(WRITE_DIR_CV_CMD);
        m.setElement(1, cv >> 8);
        m.setElement(2, cv & 0xFF);
        m.setElement(3, val);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(NCE_DIRECT_CV_TIMEOUT);
        return m;
    }

    public static NceMessage sendPacketMessage(NceTrafficController tc, byte[] bytes) {
        NceMessage m = sendPacketMessage(tc, bytes, 2);
        return m;
    }

    public static NceMessage sendPacketMessage(NceTrafficController tc, byte[] bytes, int retries) {
        // this command isn't supported by the NCE USB
        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
            log.error("attempt to send unsupported sendPacketMessage to NCE USB cmd: 0x" +
                    Integer.toHexString(SENDn_BYTES_CMD + bytes.length));
            return null;
        }
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_1999) {
            if (bytes.length < 3 || bytes.length > 6) {
                log.error("Send of NCE track packet too short or long:" +
                        Integer.toString(bytes.length) +
                        " packet:" +
                        Arrays.toString(bytes));
            }
            NceMessage m = new NceMessage(2 + bytes.length);
            m.setBinary(true);
            m.setTimeout(SHORT_TIMEOUT);
            m.setReplyLen(1);
            int i = 0; // counter to make it easier to format the message

            m.setElement(i++, SENDn_BYTES_CMD + bytes.length);
            m.setElement(i++, retries); // send this many retries. 
            for (int j = 0; j < bytes.length; j++) {
                m.setElement(i++, bytes[j] & 0xFF);
            }
            return m;
        } else {
            NceMessage m = new NceMessage(5 + 3 * bytes.length);
            m.setBinary(false);
            int i = 0; // counter to make it easier to format the message

            m.setElement(i++, 'S'); // "S C02 " means sent it twice
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
        m.setReplyLen(REPLY_1);
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
                log.error("Queue of NCE track packet too long:" +
                        Integer.toString(bytes.length) +
                        " packet :" +
                        Arrays.toString(bytes));
            }
            NceMessage m = new NceMessage(1 + bytes.length);
            m.setBinary(true);
            m.setReplyLen(REPLY_1);
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

            m.setElement(i++, 'Q'); // "S C02 " means sent it twice

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
        m.setReplyLen(REPLY_1);
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
        m.setReplyLen(REPLY_1);
        m.setTimeout(SHORT_TIMEOUT);
        byte[] mess = NceBinaryCommand.usbOpsModeAccy(accyAddr, cvAddr, cvData);
        m.setOpCode(mess[0]);
        m.setElement(1, mess[1]);
        m.setElement(2, mess[2]);
        m.setElement(3, mess[3]);
        m.setElement(4, mess[4]);
        m.setElement(5, mess[5]);
        return m;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toMonitorString(){
	    return nceMon.displayMessage(this);
    }

    private final static Logger log = LoggerFactory.getLogger(NceMessage.class);
}

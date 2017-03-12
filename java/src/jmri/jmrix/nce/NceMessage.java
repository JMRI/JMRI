package jmri.jmrix.nce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes a message to an NCE command station.
 * <P>
 * The {@link NceReply} class handles the response from the command station.
 * <P>
 * The NCE protocol has "binary" and "ASCII" command sets. Depending on the
 * version of the EPROM it contains, NCE command stations have different support
 * for command sets:
 * <UL>
 * <LI>1999 - All ASCII works. Binary works except for programming.
 * <LI>2004 - ASCII needed for programming, binary for everything else.
 * <LI>2006 - binary needed for everything
 * </UL>
 * See the {@link NceTrafficController#setCommandOptions(int)} method for more
 * information.
 * <P>
 * Apparently the binary "exitProgrammingMode" command can crash the command
 * station if the EPROM was built before 2006. This method uses a state flag
 * ({@link NceTrafficController#getNceProgMode}) to detect whether a command to
 * enter program mode has been generated, and presumably sent, when using the
 * later EPROMS.
 * <p>
 * Methods to create fixed NCE messages are in
 * {@link jmri.jmrix.nce.NceMessageUtil}.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2007
 * @author kcameron Copyright (C) 2014
 * @see jmri.jmrix.nce.NceMessageUtil
 */
public class NceMessage extends jmri.jmrix.AbstractMRMessage {

    public static final int NOP_CMD = 0x80;				//NCE NOP command
    public static final int ENTER_PROG_CMD = 0x9E;		//NCE enter programming track mode command
    public static final int EXIT_PROG_CMD = 0x9F;		//NCE exit programming track mode command
    public static final int WRITE_PAGED_CV_CMD = 0xA0;	//NCE write CV paged command
    public static final int READ_PAGED_CV_CMD = 0xA1;	//NCE read CV paged command
    public static final int WRITE_REG_CMD = 0xA6;		//NCE write register command
    public static final int READ_REG_CMD = 0xA7;		//NCE read register command
    public static final int WRITE_DIR_CV_CMD = 0xA8;	//NCE write CV direct command
    public static final int READ_DIR_CV_CMD = 0xA9;		//NCE read CV direct command
    public static final int SEND_ACC_SIG_MACRO_CMD = 0xAD;	// NCE send NMRA aspect command

    // The following commands are not supported by the NCE USB  
    public static final int ENABLE_MAIN_CMD = 0x89;		//NCE enable main track, kill programming command
    public static final int KILL_MAIN_CMD = 0x8B;		//NCE kill main track, enable programming command
    public static final int SENDn_BYTES_CMD = 0x90;		//NCE send 3 to 6 bytes (0x9n, n = 3-6) command
    public static final int QUEUEn_BYTES_CMD = 0xA0;	//NCE queue 3 to 6 bytes (0xAn, n = 3-6) command

    // The following command are only NCE USB commands
    public static final int WRITE_ACC_SIG_OP_CV_CMD = 0xAF;	//NCE USB write accessory CV

    // some constants
    protected static final int NCE_PAGED_CV_TIMEOUT = 20000;
    protected static final int NCE_DIRECT_CV_TIMEOUT = 10000;
    protected static final int SHORT_TIMEOUT = 10000;				// worst case is when loading the first panel

    public static final int REPLY_1 = 1;			// reply length of 1 byte
    public static final int REPLY_2 = 2;			// reply length of 2 byte
    public static final int REPLY_4 = 4;			// reply length of 4 byte
    public static final int REPLY_16 = 16;			// reply length of 16 bytes	

    public NceMessage() {
        super();
    }

    // create a new one
    public NceMessage(int i) {
        super(i);
    }

    // copy one
    public NceMessage(NceMessage m) {
        super(m);
        replyLen = m.replyLen;
    }

    // from String
    public NceMessage(String m) {
        super(m);
    }

    // default to expecting one reply character
    int replyLen = 1;

    /**
     * Set the number of characters expected back from the command station. Used
     * in binary mode, where there's no end-of-reply string to look for.
     *
     * @param len the number of characters expected in a reply
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

    private final static Logger log = LoggerFactory.getLogger(NceMessage.class.getName());
}

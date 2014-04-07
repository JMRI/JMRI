// NceMessage.java

package jmri.jmrix.nce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;

/**
 * Encodes a message to an NCE command station.
 * <P>
 * The {@link NceReply}
 * class handles the response from the command station.
 * <P>
 * The NCE protocol has "binary" and "ASCII" command sets.
 * Depending on the version of the EPROM it contains,
 * NCE command stations have different support for command sets:
 *<UL>
 *<LI>1999 - All ASCII works. Binary works except for programming.
 *<LI>2004 - ASCII needed for programming, binary for everything else.
 *<LI>2006 - binary needed for everything
 *</UL>
 * See the {@link NceTrafficController#setCommandOptions(int)} method for more information.
 *<P>
 * Apparently the binary "exitProgrammingMode" command can crash the 
 * command station if the EPROM was built before 2006.  This
 * method uses a state flag ({@link NceTrafficController#getNceProgMode}) to detect
 * whether a command to enter program mode has been generated, and
 * presumably sent, when using the later EPROMS.
 *
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2007
 * @version     $Revision$
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
	public static final int	SEND_ACC_SIG_MACRO_CMD = 0xAD;	// NCE send NMRA aspect command

	// The following commands are not supported by the NCE USB  
	
	public static final int ENABLE_MAIN_CMD = 0x89;		//NCE enable main track, kill programming command
	public static final int KILL_MAIN_CMD = 0x8B;		//NCE kill main track, enable programming command
	public static final int SENDn_BYTES_CMD = 0x90;		//NCE send 3 to 6 bytes (0x9n, n = 3-6) command
	public static final int QUEUEn_BYTES_CMD = 0xA0;	//NCE queue 3 to 6 bytes (0xAn, n = 3-6) command

	// The following command are only NCE USB commands
	
	public static final int	WRITE_ACC_SIG_OP_CV_CMD = 0xAF;	//NCE USB write accessory CV
	
	// some constants
	
    protected static final int NCE_PAGED_CV_TIMEOUT=20000;
    protected static final int NCE_DIRECT_CV_TIMEOUT=10000;			
    protected static final int SHORT_TIMEOUT=10000;				// worst case is when loading the first panel
    
	public static final int REPLY_1 = 1;			// reply length of 1 byte
	public static final int REPLY_2 = 2;			// reply length of 2 byte
	public static final int REPLY_4 = 4;			// reply length of 4 byte
	public static final int REPLY_16 = 16;			// reply length of 16 bytes	
    
    public NceMessage() {
        super();
    }
    
    // create a new one
    public  NceMessage(int i) {
        super(i);
    }

    // copy one
    public  NceMessage(NceMessage m) {
        super(m);
        replyLen = m.replyLen;
    }

    // from String
    public  NceMessage(String m) {
        super(m);
    }

    // default to expecting one reply character
    int replyLen = 1;    
    /**
     * Set the number of characters expected back from the 
     * command station.  Used in binary mode, where there's
     * no end-of-reply string to look for.
     */
    public void setReplyLen(int len) { 
        replyLen = len;
    }
    public int getReplyLen() { return replyLen; }

    // diagnose format
    public boolean isKillMain() {
        if (isBinary()) 
            return getOpCode() == KILL_MAIN_CMD;
        else
            return getOpCode() == 'K';
    }

    public boolean isEnableMain() {
        if (isBinary()) 
            return getOpCode() == ENABLE_MAIN_CMD;
        else
            return getOpCode() == 'E';
    }


    // static methods to return a formatted message
    public static NceMessage getEnableMain(NceTrafficController tc) {
    	// this command isn't supported by the NCE USB
    	if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE){
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
    	if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE){
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
     * enter programming track mode
     * @param tc
     */
    public static NceMessage getProgMode(NceTrafficController tc) {
		// test if supported on current connection
		if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE &&
				(tc.getCmdGroups() & NceTrafficController.CMDS_PROGTRACK) != NceTrafficController.CMDS_PROGTRACK){
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
    * Apparently the binary "exitProgrammingMode" command can crash the 
    * command station if the EPROM was built before 2006.  This
    * method uses a state flag ({@link NceTrafficController#getNceProgMode}) to detect
    * whether a command to enter program mode has been generated, and
    * presumably sent, when using the later EPROMS.
    * **/
    public static NceMessage getExitProgMode(NceTrafficController tc) {
        NceMessage m = new NceMessage(1);
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
        	// Sending exit programming mode binary can crash pre 2006 EPROMs
        	// assumption is that program mode hasn't been entered, so exit without 
        	// sending command
            if (tc.getNceProgMode() == false)
            	return null;
    		// not supported by USB connected to SB3 or PH
    		if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3
    				|| tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERHOUSE){
    			log.error("attempt to send unsupported binary command EXIT_PROG_CMD to NCE USB");
//    			return null;
    		}
    		tc.setNceProgMode(false);
            m.setBinary(true);
            m.setReplyLen(1);
            m.setOpCode(EXIT_PROG_CMD);
            m.setTimeout(SHORT_TIMEOUT);
        }
        else {
            m.setBinary(false);
            m.setOpCode('X');
            m.setTimeout(SHORT_TIMEOUT);
        }
        return m;
    }

    /**
     * Read Paged mode CV on programming track
     * @param tc
     * @param cv
     */
    public static NceMessage getReadPagedCV(NceTrafficController tc, int cv) {
		// test if supported on current connection
		if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE &&
				(tc.getCmdGroups() & NceTrafficController.CMDS_PROGTRACK) != NceTrafficController.CMDS_PROGTRACK){
			log.error("attempt to send unsupported binary command READ_PAGED_CV_CMD to NCE USB");
//			return null;
		}
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            NceMessage m = new NceMessage(3);
            m.setBinary(true);
            m.setReplyLen(2);
            m.setOpCode(READ_PAGED_CV_CMD);
            m.setElement(1,(cv >> 8));
            m.setElement(2,(cv & 0x0FF));
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
     * write paged mode CV to programming track
     * @param tc
     * @param cv
     * @param val
     */
    public static NceMessage getWritePagedCV(NceTrafficController tc, int cv, int val) {
		// test if supported on current connection
		if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE &&
				(tc.getCmdGroups() & NceTrafficController.CMDS_PROGTRACK) != NceTrafficController.CMDS_PROGTRACK){
			log.error("attempt to send unsupported binary command WRITE_PAGED_CV_CMD to NCE USB");
//			return null;
		}
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            NceMessage m = new NceMessage(4);
            m.setBinary(true);
            m.setReplyLen(1);
            m.setOpCode(WRITE_PAGED_CV_CMD);
            m.setElement(1,cv>>8);
            m.setElement(2,cv&0xFF);
            m.setElement(3,val);
            m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
            m.setTimeout(NCE_PAGED_CV_TIMEOUT);
            return m;
        } else {
            NceMessage m = new NceMessage(8);
            m.setBinary(false);
            m.setOpCode('P');
            m.addIntAsThree(cv, 1);
            m.setElement(4,' ');
            m.addIntAsThree(val, 5);
            m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
            m.setTimeout(NCE_PAGED_CV_TIMEOUT);
            return m;
        }
    }

    public static NceMessage getReadRegister(NceTrafficController tc, int reg) {
		// not supported by USB connected to SB3 or PH
		if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3
				|| tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERHOUSE){
			log.error("attempt to send unsupported binary command READ_REG_CMD to NCE USB");
			return null;
		}
        if (reg>8) log.error("register number too large: "+reg);
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            NceMessage m = new NceMessage(2);
            m.setBinary(true);
            m.setReplyLen(2);
            m.setOpCode(READ_REG_CMD);
            m.setElement(1,reg);
            m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
            m.setTimeout(NCE_PAGED_CV_TIMEOUT);
            return m;
        } else {
            NceMessage m = new NceMessage(2);
            m.setBinary(false);
            m.setOpCode('V');
            String s = ""+reg;
            m.setElement(1, s.charAt(s.length()-1));
            m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
            m.setTimeout(NCE_PAGED_CV_TIMEOUT);
            return m;
        }
    }

    public static NceMessage getWriteRegister(NceTrafficController tc, int reg, int val) {
		// not supported by USB connected to SB3 or PH
		if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3
				|| tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERHOUSE){
			log.error("attempt to send unsupported binary command WRITE_REG_CMD to NCE USB");
			return null;
		}
        if (reg>8) log.error("register number too large: "+reg);
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            NceMessage m = new NceMessage(3);
            m.setBinary(true);
            m.setReplyLen(1);
            m.setOpCode(WRITE_REG_CMD);
            m.setElement(1,reg);
            m.setElement(2,val);
            m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
            m.setTimeout(NCE_PAGED_CV_TIMEOUT);
            return m;
        } else {
            NceMessage m = new NceMessage(6);
            m.setBinary(false);
            m.setOpCode('S');
            String s = ""+reg;
            m.setElement(1, s.charAt(s.length()-1));
            m.setElement(2,' ');
            m.addIntAsThree(val, 3);
            m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
            m.setTimeout(NCE_PAGED_CV_TIMEOUT);
            return m;
        }
    }

    public static NceMessage getReadDirectCV(NceTrafficController tc, int cv) {
		// not supported by USB connected to SB3 or PH
		if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3
				|| tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERHOUSE){
			log.error("attempt to send unsupported binary command READ_DIR_CV_CMD to NCE USB");
			return null;
		}
        if (tc.getCommandOptions() < NceTrafficController.OPTION_2006){
            log.error("getReadDirectCV with option "+tc.getCommandOptions());
            return null;
        }
        NceMessage m = new NceMessage(3);
        m.setBinary(true);
        m.setReplyLen(2);
        m.setOpCode(READ_DIR_CV_CMD);
        m.setElement(1,(cv >> 8));
        m.setElement(2,(cv & 0x0FF));
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(NCE_DIRECT_CV_TIMEOUT);
        return m;
    }

    public static NceMessage getWriteDirectCV(NceTrafficController tc, int cv, int val) {
		// not supported by USB connected to SB3 or PH
		if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3
				|| tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERHOUSE){
			log.error("attempt to send unsupported binary command WRITE_DIR_CV_CMD to NCE USB");
			return null;
		}
        if (tc.getCommandOptions() < NceTrafficController.OPTION_2006)
            log.error("getWriteDirectCV with option "+tc.getCommandOptions());
        NceMessage m = new NceMessage(4);
        m.setBinary(true);
        m.setReplyLen(1);
        m.setOpCode(WRITE_DIR_CV_CMD);
        m.setElement(1,cv>>8);
        m.setElement(2,cv&0xFF);
        m.setElement(3,val);
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
    	if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE){
    		log.error("attempt to send unsupported sendPacketMessage to NCE USB cmd: 0x" + Integer.toHexString(SENDn_BYTES_CMD + bytes.length));
			return null;
    	}
		if (tc.getCommandOptions() >= NceTrafficController.OPTION_1999) {
            if (bytes.length<3 || bytes.length>6)
                log.error("Send of NCE track packet too short or long:"+Integer.toString(bytes.length)+
                    " packet:"+Arrays.toString(bytes));
            NceMessage m = new NceMessage(2+bytes.length);
            m.setBinary(true);
            m.setTimeout(SHORT_TIMEOUT);
            m.setReplyLen(1);
            int i = 0; // counter to make it easier to format the message
        
            m.setElement(i++, SENDn_BYTES_CMD + bytes.length);
            m.setElement(i++, retries);        // send this many retries. 
            for (int j = 0; j<bytes.length; j++) {
                m.setElement(i++, bytes[j]&0xFF);
            }
            return m;
        } else {
            NceMessage m = new NceMessage(5+3*bytes.length);
            m.setBinary(false);
            int i = 0; // counter to make it easier to format the message
        
            m.setElement(i++, 'S');  // "S C02 " means sent it twice
            m.setElement(i++, ' ');
            m.setElement(i++, 'C');
            m.setElement(i++, '0');
            m.setElement(i++, '2');
        
            for (int j = 0; j<bytes.length; j++) {
                m.setElement(i++, ' ');
                m.addIntAsTwoHex(bytes[j]&0xFF,i);
                i = i+2;
            }
            m.setTimeout(SHORT_TIMEOUT);
            return m;
        }
    }
    public static NceMessage createBinaryMessage(NceTrafficController tc, byte[] bytes) {
        if (tc.getCommandOptions() < NceTrafficController.OPTION_2004)
            log.error("Attempt to send NCE command to EPROM built before 2004");
        if (bytes.length<1 || bytes.length>20)
                log.error("NCE command message length error:"+ bytes.length);
        NceMessage m = new NceMessage(bytes.length);
        m.setBinary(true);
        m.setReplyLen(1);
        m.setTimeout(SHORT_TIMEOUT);
        for (int j = 0; j<bytes.length; j++) {
            m.setElement(j, bytes[j]&0xFF);
        }
        return m;
    }
    
    public static NceMessage createBinaryMessage(NceTrafficController tc, byte[] bytes, int replyLen) {
        if (tc.getCommandOptions() < NceTrafficController.OPTION_2004)
            log.error("Attempt to send NCE command to EPROM built before 2004");
        if (bytes.length<1 || bytes.length>20)
                log.error("NCE command message length error:"+ bytes.length);
        
        NceMessage m = new NceMessage(bytes.length);
        m.setBinary(true);
        m.setReplyLen(replyLen);
        m.setTimeout(SHORT_TIMEOUT);
        
        for (int j = 0; j<bytes.length; j++) {
            m.setElement(j, bytes[j]&0xFF);
        }
        return m;
    }

    public static NceMessage queuePacketMessage(NceTrafficController tc, byte[] bytes) {
    	// this command isn't supported by the NCE USB
    	if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE){
    		log.error("attempt to send unsupported queuePacketMessage to NCE USB");
			return null;
    	}
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_1999) {
            if (bytes.length<3 || bytes.length>6)
                log.error("Queue of NCE track packet too long:"+Integer.toString(bytes.length)+
                    " packet :"+Arrays.toString(bytes));
            NceMessage m = new NceMessage(1+bytes.length);
            m.setBinary(true);
            m.setReplyLen(1);
            int i = 0; // counter to make it easier to format the message
        
            m.setElement(i++, QUEUEn_BYTES_CMD + bytes.length);
            for (int j = 0; j<bytes.length; j++) {
                m.setElement(i++, bytes[j]&0xFF);
            }
            return m;
        } else {
            NceMessage m = new NceMessage(1+3*bytes.length);
            m.setBinary(false);
            int i = 0; // counter to make it easier to format the message
        
            m.setElement(i++, 'Q');  // "S C02 " means sent it twice
        
            for (int j = 0; j<bytes.length; j++) {
                m.setElement(i++, ' ');
                m.addIntAsTwoHex(bytes[j]&0xFF,i);
                i = i+2;
            }
            return m;
        }
    }

    public static NceMessage createAccySignalMacroMessage(NceTrafficController tc, int op, int addr, int data) {
        if (tc.getCommandOptions() < NceTrafficController.OPTION_2004)
            log.error("Attempt to send NCE command to EPROM built before 2004");
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

    static Logger log = LoggerFactory.getLogger(NceMessage.class.getName());
}

/* @(#)NceMessage.java */







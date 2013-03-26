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

	// The following commands are not supported by the NCE USB  
	
	public static final int ENABLE_MAIN_CMD = 0x89;		//NCE enable main track, kill programming command
	public static final int KILL_MAIN_CMD = 0x8B;		//NCE kill main track, enable programming command
	public static final int SENDn_BYTES_CMD = 0x90;		//NCE send 3 to 6 bytes (0x9n, n = 3-6) command
	public static final int QUEUEn_BYTES_CMD = 0xA0;	//NCE queue 3 to 6 bytes (0xAn, n = 3-6) command

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
    		log.error("attempt to send unsupported sendPacketMessage to NCE USB");
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
    
    /**
     * Memory offsets for cab info in a serial connected command station
     * @author kcameron
     *
     */
    public static class cabMemorySerial {

    	public final static int CS_CAB_MEM_PRO = 0x8800;	// start of NCE CS cab context page for cab 0, PowerHouse/CS2
    													// memory
    	public final static int CAB_LINE_1 = 0;		// start of first line for cab display
    	public final static int CAB_LINE_2 = 16;		// start of second line for cab display
    	public final static int CAB_SIZE = 256;		// Each cab has 256 bytes
    	public final static int CAB_CURR_SPEED = 32;	// NCE cab speed
    	public final static int CAB_ADDR_H = 33; 		// loco address, high byte
    	public final static int CAB_ADDR_L = 34; 		// loco address, low byte
    	public final static int CAB_FLAGS = 35;		// FLAGS
    	public final static int CAB_FUNC_L = 36;		// Function keys low
    	public final static int CAB_FUNC_H = 37;		// Function keys high
    	public final static int CAB_ALIAS = 38;		// Consist address
    	public final static int CAB_FUNC_13_20 = 82;	// Function keys 13 - 30
    	public final static int CAB_FUNC_21_28 = 83;	// Function keys 21 - 28
    	public final static int CAB_FLAGS1 = 101;		// NCE flag 1
    }

    /**
     * Memory offsets for cab info in a usb connected command station
     * @author kcameron
     *
     */
    public static class cabMemoryUsb {

    	public final static int CAB_LINE_1 = 0;		// start of first line for cab display
    	public final static int CAB_LINE_2 = 16;		// start of second line for cab display
    	public final static int CAB_SIZE = 256;		// Each cab has 256 bytes
    	public final static int CAB_CURR_SPEED = 32;	// NCE cab speed
    	public final static int CAB_ADDR_H = 33; 		// loco address, high byte
    	public final static int CAB_ADDR_L = 34; 		// loco address, low byte
    	public final static int CAB_FLAGS = 35;		// FLAGS
    	public final static int CAB_FUNC_L = 36;		// Function keys low
    	public final static int CAB_FUNC_H = 37;		// Function keys high
    	public final static int CAB_ALIAS = 38;		// Consist address
    	public final static int CAB_FUNC_13_20 = 99;	// Function keys 13 - 30
    	public final static int CAB_FUNC_21_28 = 100;	// Function keys 21 - 28
    	public final static int CAB_FLAGS1 = 70;		// NCE flag 1
    }
    
    public static class cmdStaMem {

    	public static final int FLAGS1_CABTYPE_DISPLAY = 0x00;	// bit 0=0, bit 7=0;
    	public static final int FLAGS1_CABTYPE_NODISP = 0x01;	// bit 0=1, bit 7=0;
    	public static final int FLAGS1_CABTYPE_USB = 0x80;		// bit 0=0, bit 7=1;
    	public static final int FLAGS1_CABTYPE_AIU = 0x81;		// bit 0=1, bit 7=1;
    	public static final int FLAGS1_CABISACTIVE = 0x02;	// if cab is active
    	public static final int FLAGS1_MASK_CABTYPE = 0x81;	// Only bits 0 and 7.
    	public static final int FLAGS1_MASK_CABISACTIVE = 0x02;	// if cab is active
    	
    	public static final int FUNC_L_F0 = 0x10;		// F0 or headlight
    	public static final int FUNC_L_F1 = 0x01;		// F1
    	public static final int FUNC_L_F2 = 0x02;		// F2
    	public static final int FUNC_L_F3 = 0x04;		// F3
    	public static final int FUNC_L_F4 = 0x08;		// F4
    	
    	public static final int FUNC_H_F5 = 0x01;		// F5
    	public static final int FUNC_H_F6 = 0x02;		// F6
    	public static final int FUNC_H_F7 = 0x04;		// F7
    	public static final int FUNC_H_F8 = 0x08;		// F8
    	public static final int FUNC_H_F9 = 0x10;		// F9
    	public static final int FUNC_H_F10 = 0x20;		// F10
    	public static final int FUNC_H_F11 = 0x40;		// F11
    	public static final int FUNC_H_F12 = 0x80;		// F12
    	
    	public static final int FUNC_H_F13 = 0x01;		// F13
    	public static final int FUNC_H_F14 = 0x02;		// F14
    	public static final int FUNC_H_F15 = 0x04;		// F15
    	public static final int FUNC_H_F16 = 0x08;		// F16
    	public static final int FUNC_H_F17 = 0x10;		// F17
    	public static final int FUNC_H_F18 = 0x20;		// F18
    	public static final int FUNC_H_F19 = 0x40;		// F10
    	public static final int FUNC_H_F20 = 0x80;		// F20
    	
    	public static final int FUNC_H_F21 = 0x01;		// F21
    	public static final int FUNC_H_F22 = 0x02;		// F22
    	public static final int FUNC_H_F23 = 0x04;		// F23
    	public static final int FUNC_H_F24 = 0x08;		// F24
    	public static final int FUNC_H_F25 = 0x10;		// F25
    	public static final int FUNC_H_F26 = 0x20;		// F26
    	public static final int FUNC_H_F27 = 0x40;		// F27
    	public static final int FUNC_H_F28 = 0x80;		// F28
    }

    static Logger log = LoggerFactory.getLogger(NceMessage.class.getName());
}

/* @(#)NceMessage.java */







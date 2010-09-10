// NceMessage.java

package jmri.jmrix.nce;
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
 * See the {@link #setCommandOptions(int)} method for more information.
 *<P>
 * Apparently the binary "exitProgrammingMode" command can crash the 
 * command station if the EPROM was built before 2006.  This
 * class uses a static state flag ({@link #ncsProgMode}) to detect
 * whether a command to enter program mode has been generated, and
 * presumably sent, when using the later EPROMS.
 *
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2007
 * @version     $Revision: 1.44 $
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
	
	public static final int ENABLE_MAIN_CMD = 0x89;		//NCE enable main trk, kill prog command
	public static final int KILL_MAIN_CMD = 0x8B;		//NCE kill main trk, enable prog command
	public static final int SENDn_BYTES_CMD = 0x90;		//NCE send 3 to 6 bytes (0x9n, n = 3-6) command
	public static final int QUEUEn_BYTES_CMD = 0xA0;	//NCE queue 3 to 6 bytes (0xAn, n = 3-6) command

	// some constants
	
    protected static final int NCE_PAGED_CV_TIMEOUT=20000;
    protected static final int NCE_DIRECT_CV_TIMEOUT=10000;			
    protected static final int SHORT_TIMEOUT=10000;				// worst case is when loading the first panel
    private static boolean ncsProgMode = false;					// Do not use exit program mode unless active
    
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
    static public NceMessage getEnableMain() {
    	// this command isn't supported by the NCE USB
    	if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE){
    		log.error("attempt to send unsupported binary command ENABLE_MAIN_CMD to NCE USB");
			return null;
    	}
        NceMessage m = new NceMessage(1);
        if (getCommandOptions() >= OPTION_1999) {
            m.setBinary(true);
            m.setReplyLen(1);
            m.setOpCode(ENABLE_MAIN_CMD);
        } else {
            m.setBinary(false);
            m.setOpCode('E');
        }
        return m;
    }

    static public NceMessage getKillMain() {
    	// this command isn't supported by the NCE USB
    	if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE){
    		log.error("attempt to send unsupported binary command KILL_MAIN_CMD to NCE USB");
			return null;
    	}
        NceMessage m = new NceMessage(1);
        if (getCommandOptions() >= OPTION_1999) {
            m.setBinary(true);
            m.setReplyLen(1);
            m.setOpCode(KILL_MAIN_CMD);
        } else {
            m.setBinary(false);
            m.setOpCode('K');
        }
        return m;
    }

    static public NceMessage getProgMode() {
		// not supported by USB connected to SB3 or PH
		if (NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_SB3
				|| NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_POWERHOUSE){
			log.error("attempt to send unsupported binary command ENTER_PROG_CMD to NCE USB");
//			return null;
		}
		NceMessage m = new NceMessage(1);
		if (getCommandOptions() >= OPTION_2006) {
			ncsProgMode = true;
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

    static public NceMessage getExitProgMode() {
        NceMessage m = new NceMessage(1);
        if (getCommandOptions() >= OPTION_2006) {
        	// Sending exit programing mode binary can crash pre 2006 EPROMs
        	// assumption is that program mode hasn't been entered, so exit without 
        	// sending command
            if (ncsProgMode == false)
            	return null;
    		// not supported by USB connected to SB3 or PH
    		if (NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_SB3
    				|| NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_POWERHOUSE){
    			log.error("attempt to send unsupported binary command EXIT_PROG_CMD to NCE USB");
//    			return null;
    		}
            ncsProgMode = false;
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

    static public NceMessage getReadPagedCV(int cv) { //Rxxx
		// not supported by USB connected to SB3 or PH
		if (NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_SB3
				|| NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_POWERHOUSE){
			log.error("attempt to send unsupported binary command READ_PAGED_CV_CMD to NCE USB");
			return null;
		}
        if (getCommandOptions() >= OPTION_2006) {
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

    static public NceMessage getWritePagedCV(int cv, int val) { //Pxxx xxx
		// not supported by USB connected to SB3 or PH
		if (NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_SB3
				|| NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_POWERHOUSE){
			log.error("attempt to send unsupported binary command WRITE_PAGED_CV_CMD to NCE USB");
//			return null;
		}
        if (getCommandOptions() >= OPTION_2006) {
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

    static public NceMessage getReadRegister(int reg) { //Vx
		// not supported by USB connected to SB3 or PH
		if (NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_SB3
				|| NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_POWERHOUSE){
			log.error("attempt to send unsupported binary command READ_REG_CMD to NCE USB");
			return null;
		}
        if (reg>8) log.error("register number too large: "+reg);
        if (getCommandOptions() >= OPTION_2006) {
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

    static public NceMessage getWriteRegister(int reg, int val) { //Sx xxx
		// not supported by USB connected to SB3 or PH
		if (NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_SB3
				|| NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_POWERHOUSE){
			log.error("attempt to send unsupported binary command WRITE_REG_CMD to NCE USB");
			return null;
		}
        if (reg>8) log.error("register number too large: "+reg);
        if (getCommandOptions() >= OPTION_2006) {
            NceMessage m = new NceMessage(3);
            m.setBinary(true);
            m.setReplyLen(2);
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

    static public NceMessage getReadDirectCV(int cv) { //Rxxx
		// not supported by USB connected to SB3 or PH
		if (NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_SB3
				|| NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_POWERHOUSE){
			log.error("attempt to send unsupported binary command READ_DIR_CV_CMD to NCE USB");
			return null;
		}
        if (getCommandOptions() < OPTION_2006)
            log.error("getReadDirectCV with option "+getCommandOptions());
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

    static public NceMessage getWriteDirectCV(int cv, int val) { //Pxxx xxx
		// not supported by USB connected to SB3 or PH
		if (NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_SB3
				|| NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_POWERHOUSE){
			log.error("attempt to send unsupported binary command WRITE_DIR_CV_CMD to NCE USB");
			return null;
		}
        if (getCommandOptions() < OPTION_2006)
            log.error("getWriteDirectCV with option "+getCommandOptions());
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
    
    static public NceMessage sendPacketMessage(byte[] bytes) {
    	NceMessage m = sendPacketMessage(bytes, 2);
    	return m;
    }
    
    static public NceMessage sendPacketMessage(byte[] bytes, int retries) {
    	// this command isn't supported by the NCE USB
    	if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE){
    		log.error("attempt to send unsupported sendPacketMessage to NCE USB");
			return null;
    	}
		if (getCommandOptions() >= OPTION_1999) {
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
    static public NceMessage createBinaryMessage(byte[] bytes) {
        if (getCommandOptions() < OPTION_2004)
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
    
    static public NceMessage createBinaryMessage(byte[] bytes, int replyLen) {
        if (getCommandOptions() < OPTION_2004)
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

    static public NceMessage queuePacketMessage(byte[] bytes) {
    	// this command isn't supported by the NCE USB
    	if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE){
    		log.error("attempt to send unsupported queuePacketMessage to NCE USB");
			return null;
    	}
        if (getCommandOptions() >= OPTION_1999) {
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
     * Create all commands in the ASCII format.
     */
    static public final int OPTION_FORCE_ASCII  = -1;
    /**
     * Create commands compatible with the 1999 EPROM.
     *<P>
     * This is binary for everything except service-mode CV programming operations.
     */
    static public final int OPTION_1999 = 0;
    /**
     * Create commands compatible with the 2004 EPROM.
     *<P>
     * This is binary for everything except service-mode CV programming operations.
     */
    static public final int OPTION_2004 = 10;
    /**
     * Create commands compatible with the 2006 EPROM.
     *<P>
     * This is binary for everything, including service-mode CV programming operations.
     */
    static public final int OPTION_2006 = 20;
    /**
     * Create all commands in the binary format.
     */
    static public final int OPTION_FORCE_BINARY = 10000;
    
    static int commandOptions = OPTION_2004;
    
    // package-level access so NceTrafficController can see it
    static boolean commandOptionSet = false;
    
    /** 
     * Control which command format should be used for various
     * commands: ASCII or binary.
     *<P>
     * The valid argument values are the class "OPTION"
     * contants, which are interpreted in the various methods to
     * get a particular message.
     *<UL>
     *<LI>{@link #OPTION_FORCE_ASCII}
     *<LI>{@link #OPTION_1999}
     *<LI>{@link #OPTION_2004}
     *<LI>{@link #OPTION_2006}
     *<LI>{@link #OPTION_FORCE_BINARY}
     *</UL>
     *
     */
     static public void setCommandOptions(int val) {
        commandOptions = val;
        if (commandOptionSet) {
            log.error("setCommandOptions called more than once");
            new Exception().printStackTrace();
        }
        commandOptionSet = true;
    }

    /** 
     * Determine which command format should be used for various
     * commands: ASCII or binary.
     *<P>
     * The valid return values are the class "OPTION"
     * contants, which are interpreted in the various methods to
     * get a particular message.
     *<UL>
     *<LI>{@link #OPTION_FORCE_ASCII}
     *<LI>{@link #OPTION_1999}
     *<LI>{@link #OPTION_2004}
     *<LI>{@link #OPTION_2006}
     *<LI>{@link #OPTION_FORCE_BINARY}
     *</UL>
     *
     */
    static public int getCommandOptions() { return commandOptions; }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NceMessage.class.getName());

}


/* @(#)NceMessage.java */







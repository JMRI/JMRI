// NceMessage.java

package jmri.jmrix.nce;

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
 * @version     $Revision: 1.28 $
 */
public class NceMessage extends jmri.jmrix.AbstractMRMessage {

    static protected int NCE_PAGED_CV_TIMEOUT=20000;
    static protected int NCE_DIRECT_CV_TIMEOUT=4000;
    static protected boolean ncsProgMode = false;				// Do not use exit program mode unless active
    
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
            return getOpCode() == 0x8B;
        else
            return getOpCode() == 'K';
    }

    public boolean isEnableMain() {
        if (isBinary()) 
            return getOpCode() == 0x89;
        else
            return getOpCode() == 'E';
    }


    // static methods to return a formatted message
    static public NceMessage getEnableMain() {
        NceMessage m = new NceMessage(1);
        if (getCommandOptions() >= OPTION_1999) {
            m.setBinary(true);
            m.setReplyLen(1);
            m.setOpCode(0x89);
        } else {
            m.setBinary(false);
            m.setOpCode('E');
        }
        return m;
    }

    static public NceMessage getKillMain() {
        NceMessage m = new NceMessage(1);
        if (getCommandOptions() >= OPTION_1999) {
            m.setBinary(true);
            m.setReplyLen(1);
            m.setOpCode(0x8B);
        } else {
            m.setBinary(false);
            m.setOpCode('K');
        }
        return m;
    }

    static public NceMessage getProgMode() {
        NceMessage m = new NceMessage(1);
        if (getCommandOptions() >= OPTION_2006) {
        	ncsProgMode = true;
            m.setBinary(true);
            m.setReplyLen(1);
            m.setOpCode(0x9E);
        }
        else {
            m.setBinary(false);
            m.setOpCode('M');
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
            ncsProgMode = false;
            m.setBinary(true);
            m.setReplyLen(1);
            m.setOpCode(0x9F);
        }
        else {
            m.setBinary(false);
            m.setOpCode('X');
        }
        return m;
    }

    static public NceMessage getReadPagedCV(int cv) { //Rxxx
        if (getCommandOptions() >= OPTION_2006) {
            NceMessage m = new NceMessage(3);
            m.setBinary(true);
            m.setReplyLen(2);
            m.setOpCode(0xA1);
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
        if (getCommandOptions() >= OPTION_2006) {
            NceMessage m = new NceMessage(4);
            m.setBinary(true);
            m.setReplyLen(1);
            m.setOpCode(0xA0);
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
        if (reg>8) log.error("register number too large: "+reg);
        if (getCommandOptions() >= OPTION_2006) {
            NceMessage m = new NceMessage(2);
            m.setBinary(true);
            m.setReplyLen(2);
            m.setOpCode(0xA7);
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
        if (reg>8) log.error("register number too large: "+reg);
        if (getCommandOptions() >= OPTION_2006) {
            NceMessage m = new NceMessage(3);
            m.setBinary(true);
            m.setReplyLen(2);
            m.setOpCode(0xA6);
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
        if (getCommandOptions() < OPTION_2006)
            log.error("getReadDirectCV with option "+getCommandOptions());
        NceMessage m = new NceMessage(3);
        m.setBinary(true);
        m.setReplyLen(2);
        m.setOpCode(0xA9);
        m.setElement(1,(cv >> 8));
        m.setElement(2,(cv & 0x0FF));
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(NCE_DIRECT_CV_TIMEOUT);
        return m;
    }

    static public NceMessage getWriteDirectCV(int cv, int val) { //Pxxx xxx
        if (getCommandOptions() < OPTION_2006)
            log.error("getWriteDirectCV with option "+getCommandOptions());
        NceMessage m = new NceMessage(4);
        m.setBinary(true);
        m.setReplyLen(1);
        m.setOpCode(0xA8);
        m.setElement(1,cv>>8);
        m.setElement(2,cv&0xFF);
        m.setElement(3,val);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(NCE_DIRECT_CV_TIMEOUT);
        return m;
    }
    
    static public NceMessage sendPacketMessage(byte[] bytes) {
        if (getCommandOptions() >= OPTION_1999) {
            if (bytes.length<3 || bytes.length>6)
                log.error("Send of NCE track packet too short or long:"+(bytes.length)+
                    " packet:"+bytes);
            NceMessage m = new NceMessage(2+bytes.length);
            m.setBinary(true);
            m.setReplyLen(1);
            int i = 0; // counter to make it easier to format the message
        
            m.setElement(i++, 0x90+bytes.length);
            m.setElement(i++, 0x02);        // send twice
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
        m.setTimeout(NCE_PAGED_CV_TIMEOUT);
        
        for (int j = 0; j<bytes.length; j++) {
            m.setElement(j, bytes[j]&0xFF);
        }
        return m;
    }

    static public NceMessage queuePacketMessage(byte[] bytes) {
        if (getCommandOptions() >= OPTION_1999) {
            if (bytes.length<3 || bytes.length>6)
                log.error("Queue of NCE track packet too long:"+(bytes.length)+
                    " packet:"+bytes);
            NceMessage m = new NceMessage(1+bytes.length);
            m.setBinary(true);
            m.setReplyLen(1);
            int i = 0; // counter to make it easier to format the message
        
            m.setElement(i++, 0xA0+bytes.length);
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
    static public int getCommandOptions() { return commandOptions; }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceMessage.class.getName());

}


/* @(#)NceMessage.java */







// NceMessage.java

package jmri.jmrix.nce;

/**
 * Encodes a message to an NCE command station.
 * <P>
 * The {@link NceReply}
 * class handles the response from the command station.
 *
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @version     $Revision: 1.11 $
 */
public class NceMessage extends jmri.jmrix.AbstractMRMessage {

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

    int replyLen;    
    /**
     * Set the number of characters expected back from the 
     * command station.  Used in binary mode, where there's
     * no end-of-reply string to look for
     */
    public void setReplyLen(int len) { replyLen = len; }
    public int getReplyLen() { return replyLen; }

    // diagnose format
    public boolean isKillMain() {
        return getOpCode() == 'K';
    }

    public boolean isEnableMain() {
        return getOpCode() == 'E';
    }


    // static methods to return a formatted message
    static public NceMessage getEnableMain() {
        NceMessage m = new NceMessage(1);
        m.setBinary(false);
        m.setOpCode('E');
        return m;
    }

    static public NceMessage getKillMain() {
        NceMessage m = new NceMessage(1);
        m.setBinary(false);
        m.setOpCode('K');
        return m;
    }

    static public NceMessage getProgMode() {
        NceMessage m = new NceMessage(1);
        m.setBinary(false);
        m.setOpCode('M');
        return m;
    }

    static public NceMessage getExitProgMode() {
        NceMessage m = new NceMessage(1);
        m.setBinary(false);
        m.setOpCode('X');
        return m;
    }

    static public NceMessage getReadPagedCV(int cv) { //Rxxx
        NceMessage m = new NceMessage(4);
        m.setBinary(false);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(LONG_TIMEOUT);
        m.setOpCode('R');
        m.addIntAsThree(cv, 1);
        return m;
    }

    static public NceMessage getWritePagedCV(int cv, int val) { //Pxxx xxx
        NceMessage m = new NceMessage(8);
        m.setBinary(false);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(LONG_TIMEOUT);
        m.setOpCode('P');
        m.addIntAsThree(cv, 1);
        m.setElement(4,' ');
        m.addIntAsThree(val, 5);
        return m;
    }

    static public NceMessage getReadRegister(int reg) { //Vx
        if (reg>8) log.error("register number too large: "+reg);
        NceMessage m = new NceMessage(2);
        m.setBinary(false);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(LONG_TIMEOUT);
        m.setOpCode('V');
        String s = ""+reg;
        m.setElement(1, s.charAt(s.length()-1));
        return m;
    }

    static public NceMessage getWriteRegister(int reg, int val) { //Sx xxx
        if (reg>8) log.error("register number too large: "+reg);
        NceMessage m = new NceMessage(6);
        m.setBinary(false);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(LONG_TIMEOUT);
        m.setOpCode('S');
        String s = ""+reg;
        m.setElement(1, s.charAt(s.length()-1));
        m.setElement(2,' ');
        m.addIntAsThree(val, 3);
        return m;
    }
    
    static public NceMessage sendPacketMessage(byte[] bytes) {
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

    static public NceMessage queuePacketMessage(byte[] bytes) {
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceMessage.class.getName());

}


/* @(#)NceMessage.java */

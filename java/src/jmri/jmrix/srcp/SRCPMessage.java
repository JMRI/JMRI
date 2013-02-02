// SRCPMessage.java

package jmri.jmrix.srcp;

import org.apache.log4j.Logger;

/**
 * Encodes a message to an SRCP server.  The SRCPReply
 * class handles the response from the command station.
 * <P>
 * The {@link SRCPReply}
 * class handles the response from the command station.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001, 2004, 2008
 * @version			$Revision$
 */
public class SRCPMessage extends jmri.jmrix.AbstractMRMessage {

    public SRCPMessage() {
        super();
    }

    // create a new one
    public  SRCPMessage(int i) {
        super(i);
    }

    // copy one
    public  SRCPMessage(SRCPMessage m) {
        super(m);
    }

    // from String
    public  SRCPMessage(String m) {
        super(m);
    }

    // diagnose format
    public boolean isKillMain() {
        String s = toString();
        return s.contains("POWER OFF") && s.contains("SET");
    }

    public boolean isEnableMain() {
        String s = toString();
        return s.contains("POWER ON") && s.contains("SET");
    }

    // static methods to return a formatted message
    static public SRCPMessage getEnableMain() {
        SRCPMessage m = new SRCPMessage("SET 1 POWER ON\n");
        m.setBinary(false);
        return m;
    }

    static public SRCPMessage getKillMain() {
        SRCPMessage m = new SRCPMessage("SET 1 POWER OFF\n");
        m.setBinary(false);
        return m;
    }
    
    /* 
     * get a static message to add a locomotive to a Standard Consist 
     * in the normal direction
     * @param ConsistAddress - a consist address in the range 1-255
     * @param LocoAddress - a jmri.DccLocoAddress object representing the 
     * locomotive to add
     * @return an SRCPMessage of the form GN cc llll 
     */
    static public SRCPMessage getAddConsistNormal(int ConsistAddress,jmri.DccLocoAddress LocoAddress) {
        SRCPMessage m = new SRCPMessage(10);
        m.setBinary(false);
        m.setOpCode('G');
        m.setElement(1,'N');
        m.setElement(2,' ');
        m.addIntAsTwoHex(ConsistAddress, 3);
        m.setElement(5,' ');
        m.addIntAsFourHex(LocoAddress.getNumber(), 6);
        return m;
    }

    /* 
     * get a static message to add a locomotive to a standard consist in 
     * the reverse direction
     * @param ConsistAddress - a consist address in the range 1-255
     * @param LocoAddress - a jmri.DccLocoAddress object representing the 
     * locomotive to add
     * @return an SRCPMessage of the form GS cc llll 
     */
    static public SRCPMessage getAddConsistReverse(int ConsistAddress,jmri.DccLocoAddress LocoAddress) {
        SRCPMessage m = new SRCPMessage(10);
        m.setBinary(false);
        m.setOpCode('G');
        m.setElement(1,'R');
        m.setElement(2,' ');
        m.addIntAsTwoHex(ConsistAddress, 3);
        m.setElement(5,' ');
        m.addIntAsFourHex(LocoAddress.getNumber(), 6);
        return m;
    }

    /* 
     * get a static message to subtract a locomotive from a Standard Consist
     * @param ConsistAddress - a consist address in the range 1-255
     * @param LocoAddress - a jmri.DccLocoAddress object representing the 
     * locomotive to remove
     * @return an SRCPMessage of the form GS cc llll 
     */
    static public SRCPMessage getSubtractConsist(int ConsistAddress,jmri.DccLocoAddress LocoAddress) {
        SRCPMessage m = new SRCPMessage(10);
        m.setBinary(false);
        m.setOpCode('G');
        m.setElement(1,'S');
        m.setElement(2,' ');
        m.addIntAsTwoHex(ConsistAddress, 3);
        m.setElement(5,' ');
        m.addIntAsFourHex(LocoAddress.getNumber(), 6);
        return m;
    }

    /* 
     * get a static message to delete a standard consist
     * @param ConsistAddress - a consist address in the range 1-255
     * @return an SRCPMessage of the form GK cc 
     */
    static public SRCPMessage getKillConsist(int ConsistAddress) {
        SRCPMessage m = new SRCPMessage(5);
        m.setBinary(false);
        m.setOpCode('G');
        m.setElement(1,'K');
        m.setElement(2,' ');
        m.addIntAsTwoHex(ConsistAddress, 3);
        return m;
    }

    /* 
     * get a static message to display a standard consist
     * @param ConsistAddress - a consist address in the range 1-255
     * @return an SRCPMessage of the form GD cc 
     */
    static public SRCPMessage getDisplayConsist(int ConsistAddress) {
        SRCPMessage m = new SRCPMessage(5);
        m.setBinary(false);
        m.setOpCode('G');
        m.setElement(1,'D');
        m.setElement(2,' ');
        m.addIntAsTwoHex(ConsistAddress, 3);
        return m;
    }

    static public SRCPMessage getProgMode() {
	String msg = "INIT 1 SM NMRA\n";
	SRCPMessage m = new SRCPMessage(msg);
        return m;
    }

    static public SRCPMessage getExitProgMode() {
	String msg = "TERM 1 SM\n";
	SRCPMessage m = new SRCPMessage(msg);
        return m;
    }

    static public SRCPMessage getReadDirectCV(int cv) {
	String msg = "GET 1 SM 0 CV " + cv + "\n";
	SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;
    }

    static public SRCPMessage getConfirmDirectCV(int cv, int val) {
	String msg = "VERIFY 1 SM 0 CV " + cv + " " + val + "\n";
	SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;
    
    }

    static public SRCPMessage getWriteDirectCV(int cv, int val) {
	String msg = "SET 1 SM 0 CV " + cv + " " + val + "\n";
	SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;
    }

    static public SRCPMessage getReadDirectBitCV(int cv,int bit) {
	String msg = "GET 1 SM 0 CVBIT " + cv + " " + bit +"\n";
	SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;
    }

    static public SRCPMessage getConfirmDirectBitCV(int cv, int bit, int val) {
	String msg = "VERIFY 1 SM 0 CV " + cv + " " + bit + " " + val + "\n";
	SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;
    
    }

    static public SRCPMessage getWriteDirectBitCV(int cv, int bit, int val) {
	String msg = "SET 1 SM 0 CV " + cv + " " + bit + " " + val + "\n";
	SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;
    }

    static public SRCPMessage getReadRegister(int reg) {
        if (reg>8) log.error("register number too large: "+reg);
	String msg = "GET 1 SM 0 REG " + reg + "\n";
	SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;
    }

    static public SRCPMessage getConfirmRegister(int reg, int val) {
        if (reg>8) log.error("register number too large: "+reg);
	String msg = "VERIFY 1 SM 0 REG " + reg + " " + val + "\n";
	SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;
    }

    static public SRCPMessage getWriteRegister(int reg, int val) {
        if (reg>8) log.error("register number too large: "+reg);
	String msg = "SET 1 SM 0 REG " + reg + " " + val + "\n";
	SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;
    }

    static final int LONG_TIMEOUT=180000;  // e.g. for programming options

    static Logger log = Logger.getLogger(SRCPMessage.class.getName());

}

/* @(#)SRCPMessage.java */

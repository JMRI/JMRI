// EasyDccMessage.java

package jmri.jmrix.easydcc;

/**
 * Encodes a message to an EasyDCC command station.  The EasyDccReply
 * class handles the response from the command station.
 * <P>
 * The {@link EasyDccReply}
 * class handles the response from the command station.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001, 2004
 * @version			$Revision: 1.12 $
 */
public class EasyDccMessage extends jmri.jmrix.AbstractMRMessage {

    public EasyDccMessage() {
        super();
    }

    // create a new one
    public  EasyDccMessage(int i) {
        super(i);
    }

    // copy one
    public  EasyDccMessage(EasyDccMessage m) {
        super(m);
    }

    // from String
    public  EasyDccMessage(String m) {
        super(m);
    }

    // diagnose format
    public boolean isKillMain() {
        return getOpCode() == 'K';
    }

    public boolean isEnableMain() {
        return getOpCode() == 'E';
    }


    // static methods to return a formatted message
    static public EasyDccMessage getEnableMain() {
        EasyDccMessage m = new EasyDccMessage(1);
        m.setBinary(false);
        m.setOpCode('E');
        return m;
    }

    static public EasyDccMessage getKillMain() {
        EasyDccMessage m = new EasyDccMessage(1);
        m.setBinary(false);
        m.setOpCode('K');
        return m;
    }

    static public EasyDccMessage getProgMode() {
        EasyDccMessage m = new EasyDccMessage(1);
        m.setBinary(false);
        m.setOpCode('M');
        return m;
    }

    static public EasyDccMessage getExitProgMode() {
        EasyDccMessage m = new EasyDccMessage(1);
        m.setBinary(false);
        m.setOpCode('X');
        return m;
    }

    static public EasyDccMessage getReadPagedCV(int cv) { //R xxx
        EasyDccMessage m = new EasyDccMessage(5);
        m.setBinary(false);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(LONG_TIMEOUT);
        m.setOpCode('R');
        m.setElement(1,' ');
        m.addIntAsThreeHex(cv, 2);
        return m;
    }

    static public EasyDccMessage getWritePagedCV(int cv, int val) { //P xxx xx
        EasyDccMessage m = new EasyDccMessage(8);
        m.setBinary(false);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(LONG_TIMEOUT);
        m.setOpCode('P');
        m.setElement(1,' ');
        m.addIntAsThreeHex(cv, 2);
        m.setElement(5,' ');
        m.addIntAsTwoHex(val, 6);
        return m;
    }

    static public EasyDccMessage getReadRegister(int reg) { //Vx
        if (reg>8) log.error("register number too large: "+reg);
        EasyDccMessage m = new EasyDccMessage(2);
        m.setBinary(false);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(LONG_TIMEOUT);
        m.setOpCode('V');
        String s = ""+reg;
        m.setElement(1, s.charAt(s.length()-1));
        return m;
    }

    static public EasyDccMessage getWriteRegister(int reg, int val) { //Sx xx
        if (reg>8) log.error("register number too large: "+reg);
        EasyDccMessage m = new EasyDccMessage(5);
        m.setBinary(false);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(LONG_TIMEOUT);
        m.setOpCode('S');
        String s = ""+reg;
        m.setElement(1, s.charAt(s.length()-1));
        m.setElement(2,' ');
        m.addIntAsTwoHex(val, 3);
        return m;
    }

    static protected int LONG_TIMEOUT=180000;  // e.g. for programming options

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccMessage.class.getName());

}


/* @(#)EasyDccMessage.java */


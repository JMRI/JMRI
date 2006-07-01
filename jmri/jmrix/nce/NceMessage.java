// NceMessage.java

package jmri.jmrix.nce;

/**
 * Encodes a message to an NCE command station.  The NceReply
 * class handles the response from the command station.
 *
 * @author	        Bob Jacobsen  Copyright (C) 2001
 * @version             $Revision: 1.9 $
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
    }

    // from String
    public  NceMessage(String m) {
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceMessage.class.getName());

}


/* @(#)NceMessage.java */

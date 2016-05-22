// EcosMessage.java

package jmri.jmrix.ecos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes a message to an Ecos command station.
 * <P>
 * The {@link EcosReply}
 * class handles the response from the command station.
 * <P>
 *
 * @author	Bob Jacobsen  Copyright (C) 2001, 2008
 * @author Daniel Boudreau Copyright (C) 2007
 * @version     $Revision$
 */
public class EcosMessage extends jmri.jmrix.AbstractMRMessage {
	
    
    public EcosMessage() {
        super();
    }
    
    // create a new one
    public  EcosMessage(int i) {
        super(i);
    }

    // copy one
    public  EcosMessage(EcosMessage m) {
        super(m);
    }

    // from String
    public  EcosMessage(String m) {
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
    static public EcosMessage getEnableMain() {
        EcosMessage m = new EcosMessage(1);
        m.setBinary(false);
        m.setOpCode('E');
        return m;
    }

    static public EcosMessage getKillMain() {
        EcosMessage m = new EcosMessage(1);
        m.setBinary(false);
        m.setOpCode('K');
        return m;
    }

    static public EcosMessage getProgMode() {
		EcosMessage m = new EcosMessage(1);
        m.setBinary(false);
        m.setOpCode('M');
        m.setTimeout(SHORT_TIMEOUT);
		return m;
	}

    static public EcosMessage getExitProgMode() {
        EcosMessage m = new EcosMessage(1);
        m.setBinary(false);
        m.setOpCode('X');
        m.setTimeout(SHORT_TIMEOUT);
        return m;
    }

    static public EcosMessage getReadPagedCV(int cv) { //Rxxx
        EcosMessage m = new EcosMessage(4);
        m.setBinary(false);
        m.setOpCode('R');
        m.addIntAsThree(cv, 1);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        //m.setTimeout(NCE_PAGED_CV_TIMEOUT);
        return m;
    }

    static public EcosMessage getWritePagedCV(int cv, int val) { //Pxxx xxx
        EcosMessage m = new EcosMessage(8);
        m.setBinary(false);
        m.setOpCode('P');
        m.addIntAsThree(cv, 1);
        m.setElement(4,' ');
        m.addIntAsThree(val, 5);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        //m.setTimeout(NCE_PAGED_CV_TIMEOUT);
        return m;
    }

    static public EcosMessage getReadRegister(int reg) { //Vx
        if (reg>8) log.error("register number too large: "+reg);
        EcosMessage m = new EcosMessage(2);
        m.setBinary(false);
        m.setOpCode('V');
        String s = ""+reg;
        m.setElement(1, s.charAt(s.length()-1));
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        //m.setTimeout(NCE_PAGED_CV_TIMEOUT);
        return m;
    }

    static public EcosMessage getWriteRegister(int reg, int val) { //Sx xxx
        if (reg>8) log.error("register number too large: "+reg);
        EcosMessage m = new EcosMessage(6);
        m.setBinary(false);
        m.setOpCode('S');
        String s = ""+reg;
        m.setElement(1, s.charAt(s.length()-1));
        m.setElement(2,' ');
        m.addIntAsThree(val, 3);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        //m.setTimeout(NCE_PAGED_CV_TIMEOUT);
        return m;
    }

    static public EcosMessage getReadDirectCV(int cv) { //Rxxx
        EcosMessage m = new EcosMessage(3);
        m.setBinary(true);
        //m.setOpCode(READ_DIR_CV_CMD);
        m.setElement(1,(cv >> 8));
        m.setElement(2,(cv & 0x0FF));
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        //m.setTimeout(NCE_DIRECT_CV_TIMEOUT);
        return m;
    }

    static public EcosMessage getWriteDirectCV(int cv, int val) { //Pxxx xxx
        EcosMessage m = new EcosMessage(4);
        m.setBinary(true);
        //m.setOpCode(WRITE_DIR_CV_CMD);
        m.setElement(1,cv>>8);
        m.setElement(2,cv&0xFF);
        m.setElement(3,val);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        //m.setTimeout(NCE_DIRECT_CV_TIMEOUT);
        return m;
    }
    
    static public EcosMessage sendPacketMessage(byte[] bytes) {
    	EcosMessage m = sendPacketMessage(bytes, 2);
    	return m;
    }
    
    static public EcosMessage sendPacketMessage(byte[] bytes, int retries) {
        EcosMessage m = new EcosMessage(5+3*bytes.length);
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
    static public EcosMessage createBinaryMessage(byte[] bytes) {
        if (bytes.length<1 || bytes.length>20)
                log.error("ECOS command message length error:"+ bytes.length);
        EcosMessage m = new EcosMessage(bytes.length);
        m.setBinary(true);
        m.setTimeout(SHORT_TIMEOUT);
        for (int j = 0; j<bytes.length; j++) {
            m.setElement(j, bytes[j]&0xFF);
        }
        return m;
    }
    
    static public EcosMessage queuePacketMessage(byte[] bytes) {
        EcosMessage m = new EcosMessage(1+3*bytes.length);
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

    static Logger log = LoggerFactory.getLogger(EcosMessage.class.getName());
}

/* @(#)EcosMessage.java */







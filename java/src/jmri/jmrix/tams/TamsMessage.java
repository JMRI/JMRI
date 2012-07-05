// TamsMessage.java

package jmri.jmrix.tams;

/**
 * Encodes a message to an Tams command station.
 * <P>
 * The {@link TamsReply}
 * class handles the response from the command station.
 * <P>
 *
 * Based on work by Bob Jacobsen
 * @author	Kevin Dickerson  Copyright (C) 2012
 * @version     $Revision: 17977 $
 */
public class TamsMessage extends jmri.jmrix.AbstractMRMessage {
	
    static private final int TamsProgrammingTimeout = 10000;
    
    public TamsMessage() {
        super();
    }
    
    // create a new one
    public  TamsMessage(int i) {
        super(i);
    }

    // copy one
    public  TamsMessage(TamsMessage m) {
        super(m);
    }

    // from String
    public  TamsMessage(String m) {
        super(m);
    }
    
    public TamsMessage(byte [] packet ) {
    	this((packet.length));
        int i = 0; // counter of byte in output message
        int j = 0; // counter of byte in input packet
        setBinary(true);
        // add each byte of the input message
        for (j=0; j<packet.length; j++) {
            this.setElement(i, packet[i]);
            i++;
        }
        setRetries(1);
    }

    static public final int POLLTIMEOUT = 100;

    static public TamsMessage getReadPagedCV(int cv) { //Rxxx
        TamsMessage m = new TamsMessage("xPTRP " + cv);
       // m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        return m;
    }

    static public TamsMessage getWritePagedCV(int cv, int val) { //Pxxx xxx
        TamsMessage m = new TamsMessage("xPTWP " + cv + ", " + val);
      //  m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        return m;
    }

    static public TamsMessage getReadRegister(int reg) { //Vx
        TamsMessage m = new TamsMessage("xPTRR " + reg);
      //  m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        return m;
    }

    static public TamsMessage getWriteRegister(int reg, int val) { //Sx xxx
        TamsMessage m = new TamsMessage("xPTWR " + reg + ", " + val);
      //  m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        return m;
    }

    static public TamsMessage getReadDirectByteCV(int cv) { //Rxxx
        TamsMessage m = new TamsMessage("xPTRD " + cv);
      //  m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        return m;
    }

    static public TamsMessage getWriteDirectByteCV(int cv, int val) { //Pxxx xxx
        TamsMessage m = new TamsMessage("xPTWD " + cv + ", " + val);
       // m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        return m;
    }
    
    static public TamsMessage getReadDirectBitCV(int cv) { //Rxxx
        TamsMessage m = new TamsMessage("xPTRB " + cv);
      //  m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        return m;
    }
    
    static public TamsMessage getWriteDirectBitCV(int cv, int bit, int val) { //Pxxx xxx
        TamsMessage m = new TamsMessage("xPTWB " + cv + ", " + bit + ", " + val);
      //  m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        return m;
    }
    
    static public TamsMessage getWriteOpsModeCVMsg(int adr, int cv, int val) { //Pxxx xxx
        TamsMessage m = new TamsMessage("xPD " + adr + ", " + cv + ", " + val);
      //  m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        return m;
    }
    
    static public TamsMessage getWriteOpsModeAccCVMsg(int adr, int cv, int val) { //Pxxx xxx
        TamsMessage m = new TamsMessage("xPA " + adr + ", " + cv + ", " + val);
      //  m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(TamsProgrammingTimeout);
        return m;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TamsMessage.class.getName());
}

/* @(#)TamsMessage.java */







// Dcc4PcMessage.java

package jmri.jmrix.dcc4pc;

import org.apache.log4j.Logger;

/**
 * Encodes a message to the DCC4PC Interface.
 * <P>
 * The {@link Dcc4PcReply}
 * class handles the response from the command station.
 *
 * @author  Bob Jacobsen  Copyright (C) 2001
 * @author  Kevin Dickerson Copyright (C) 2012
 * @version $Revision: 18133 $
 */
public class Dcc4PcMessage  extends jmri.jmrix.AbstractMRMessage {
    
    public static final int MAXSIZE = 515;
    
    // create a new one
    public  Dcc4PcMessage(int i) {
        if (i<1)
            log.error("invalid length in call to ctor");
        _nDataChars = i;
        _dataChars = new int[i];
        setBinary(true);
        setRetries(3);
    }
    
    /**
     * Creates a new Dcc4PcMessage containing a byte array to represent
     * a packet to output
     * @param packet The contents of the packet
     */
    public Dcc4PcMessage(byte [] packet ) {
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
        
        
    // from String
    public Dcc4PcMessage(String s) {
            _nDataChars = s.length();
            _dataChars = new int[_nDataChars];
            for (int i = 0; i<_nDataChars; i++)
                    _dataChars[i] = s.charAt(i);
        setBinary(true);
        setRetries(3);
    }

    // copy one
    @SuppressWarnings("null")
	public  Dcc4PcMessage(Dcc4PcMessage m) {
        if (m == null)
            log.error("copy ctor of null message");
        _nDataChars = m._nDataChars;
        _dataChars = new int[_nDataChars];
        for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m._dataChars[i];
        setBinary(true);
        setRetries(1);
    }
    
    public void setForChildBoard(boolean boo){
        childBoard = boo;
    }
    
    boolean childBoard = false;
    
    public boolean isForChildBoard(){
        return childBoard;
    }

    
    public void setElement(int n, int v) {
      _dataChars[n] = v;
    }
    
    public String toHexString() {
        
        StringBuffer buf = new StringBuffer();
        buf.append("0x" + Integer.toHexString(0xFF & _dataChars[0]));
        for (int i=1; i<_nDataChars; i++) {
            //s+=;
            buf.append(", 0x" + Integer.toHexString(0xFF & _dataChars[i]));
        }
        return buf.toString();
    }

    /**
     * Get formatted message for direct output to stream - this is the final 
     * format of the message as a byte array
     * @return the formatted message as a byte array
     */
    public byte[] getFormattedMessage() {
	    int len = this.getNumDataElements();
	    // space for carriage return if required
	    int cr = 0;
	
	    byte msg[] = new byte[len+cr];
	
	    for (int i=0; i< len; i++){
	      msg[i] = (byte) this.getElement(i);
        }
	    return msg;
    }
    //Not supported
    static public Dcc4PcMessage getProgMode() {
        Dcc4PcMessage m = new Dcc4PcMessage(1);
        //m.setOpCode('+');
        return m;
	}

    //Not supported
    static public Dcc4PcMessage getExitProgMode() {
        Dcc4PcMessage m = new Dcc4PcMessage(1);
       // m.setOpCode(' ');
        return m;
    }
    
    boolean response = false;
    
    public boolean isGetResponse(){ return response; }
    
    static public Dcc4PcMessage getInfo(int address){
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[] {(byte)0x0b, (byte)address, (byte)0x00});
        m.childBoard = true;
        return m;
    }
    
    static public Dcc4PcMessage getDescription(int address){
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[] {(byte)0x0b, (byte)address, (byte)0x01});
        m.childBoard = true;
        return m;
    }
    
    static public Dcc4PcMessage getSerialNumber(int address){
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[] {(byte)0x0b, (byte)address, (byte)0x02});
        m.childBoard = true;
        return m;
    }
    
    static public Dcc4PcMessage resetBoardData(int address){
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[] {(byte)0x0b, (byte)address, (byte)0x09});
        m.childBoard = true;
        return m;
    }    
    
    static public Dcc4PcMessage getEnabledInputs(int address){
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[] {(byte)0x0b, (byte)address, (byte)0x07});
        m.childBoard = true;
        return m;
    }
    
    static public Dcc4PcMessage getInfo(){
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[] {(byte)0x00});
        return m;
    }
    
    static public Dcc4PcMessage getResponse(){
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[] {(byte)0x0C});
        m.response=true;
        return m;
    }
    
    static public Dcc4PcMessage getDescription(){
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[] {(byte)0x01});
        return m;
    }
    
    static public Dcc4PcMessage getSerialNumber(){
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[] {(byte)0x02});
        return m;
    }
    
    static Logger log = Logger.getLogger(Dcc4PcMessage.class.getName());

}

/* @(#)Dcc4PcMessage.java */

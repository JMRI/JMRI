// XpaMessage.java

package jmri.jmrix.xpa;

/**
 * Encodes a message to an XPressNet command station via an XPA and a modem.
 * <P>
 * The {@link XpaReply}
 * class handles the response from the command station.
 *
 * @author	Paul Bender  Copyright (C) 2004
 * @version	$Revision: 1.2 $
 */
public class XpaMessage {

    public static int maxSize = 64;
    
    private int _nDataChars = 0;
    private byte _dataChars[] = null;

    // create a new one
    public  XpaMessage(int i) {
        if (i<1)
            log.error("invalid length in call to ctor");
        _nDataChars = i;
        _dataChars = new byte[i];
    }

    // create a new one, given a string containing the message.
    public  XpaMessage(String S) {
        if (S.length()<1)
            log.error("zero length string in call to ctor");
        _nDataChars = S.length();
        _dataChars = S.getBytes();
    }

    // create a new one with default maxSize
    public  XpaMessage() {
	this(maxSize);
    }

    // copy one
    public  XpaMessage(XpaMessage m) {
        if (m == null)
            log.error("copy ctor of null message");
        _nDataChars = m._nDataChars;
        _dataChars = new byte[_nDataChars];
        for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m._dataChars[i];
    }

    public void setOpCode(int i) { _dataChars[0]=(byte)i;}
    public int getOpCode() {return (int)_dataChars[0];}
    public String getOpCodeHex() { return "0x"+Integer.toHexString(getOpCode()); }

    // accessors to the bulk data
    public int getNumDataElements() {return _nDataChars;}
    public int getElement(int n) {return _dataChars[n];}
    public void setElement(int n, int v) { _dataChars[n] = (byte)(v&0x7F); }

    // display format
    public String toString() {
        String s = "";
        for (int i=0; i<_nDataChars; i++) {
            s+=(char)_dataChars[i];
        }
        return s;
    }

    // static methods to return a formatted message

    static XpaMessage getDefaultInitMsg() {
	XpaMessage m=new XpaMessage("ATX0E0;");
	return m;
    }


    /* Get a message which sends an Estop or Everything off command
       to the layout.  This will toggle the Estop commands.  
       XPA settings can change the behavior of this command.  It may 
       only work with a single locomotive, or it may kill the entire 
       layout. 
    */
    static XpaMessage getEStopMsg(){
	XpaMessage m=new XpaMessage("ATDT0;");
	return m;
    }

    // Locomotive Messages

    /* 
       Get a message which sends an "Idle" (zero speed) command
       to a specific locomotive on the layout.  
    */
    static XpaMessage getIdleMsg(int Address){
	XpaMessage m=new XpaMessage("ATDT#" + Address +"*5;");
	return m;
    }

    /* 
       Get a message for an "Increase Speed" command
       to a specific locomotive on the layout.  To make 
       calculations easy, this uses a single speed step increase
    */
    static XpaMessage getIncSpeedMsg(int Address,int steps){
	String Message=new String("ATDT#" +Address+ "*");
	for(int i=0;i<steps;i++)
		Message = Message + "3";
	Message = Message +";";
	XpaMessage m=new XpaMessage(Message);
	return m;
    }

    /* 
       Get a message for a "Decrease Speed" command
       to a specific locomotive on the layout.  To make 
       calculations easy, this uses a single speed step increase
    */
    static XpaMessage getDecSpeedMsg(int Address, int steps){
	String Message=new String("ATDT#" +Address+ "*");
	for(int i=0;i<steps;i++)
		Message = Message + "1";
	Message = Message +";";
	XpaMessage m=new XpaMessage(Message);
	return m;
    }

    /* 
       Get a message for a "Direction Forward" command
       to a specific locomotive on the layout.  
    */
    static XpaMessage getDirForwardMsg(int Address){
	XpaMessage m=new XpaMessage("ATDT#" + Address +"*02;");
	return m;
    }

    /* 
       Get a message for a "Direction Reverse" command
       to a specific locomotive on the layout.  
    */
    static XpaMessage getDirReverseMsg(int Address){
	XpaMessage m=new XpaMessage("ATDT#" + Address +"*08;");
	return m;
    }

    /* 
       Get a message which sends a "Toggle Function" command
       to a specific locomotive on the layout.  
    */
    static XpaMessage getFunctionMsg(int Address, int Function){
	XpaMessage m=new XpaMessage("ATDT#" + Address +"**" +Function + ";");
	return m;
    }

    // Switch Commands

    /* 
       Get a message for a "Switch Possition Normal" command
       to a specific accessory decoder on the layout.  
    */
    static XpaMessage getSwitchNormalMsg(int Address){
	XpaMessage m=new XpaMessage("ATDT#" + Address +"#3;");
	return m;
    }

    /* 
       Get a message for a "Switch Possition Reverse" command
       to a specific accessory decoder on the layout.  
    */
    static XpaMessage getSwitchReverseMsg(int Address){
	XpaMessage m=new XpaMessage("ATDT#" + Address +"#1;");
	return m;
    }



    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XpaMessage.class.getName());

}

/* @(#)XpaMessage.java */

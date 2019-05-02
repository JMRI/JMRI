package jmri.jmrix.tams;

/**
 * Constants to represent values seen in Tams traffic
 *
 * @author  Jan Boen 151216
 * @author Based on work from Kevin Dickerson
 */
public final class TamsConstants {

    // Various bit masks
    public final static int XPWRMASK = 0x08;  //bit 3 of XStatus 
    public final static int XSENMASK = 0x04;  //bit 2 of XEvent
    public final static int XLOKMASK = 0x01;  //bit 0 of XEvent
    public final static int XTRNMASK = 0x18;  //bits 4 and 5 of XEvent

    // Various other elements
    public final static int EOM80 = 0x80;  //80h as end of message
    public final static int EOM00 = 0x00;  //00h as end of message 
    public final static int POLLMSG = 0x00;  //first byte for a Poll related TamsMessage 
    public final static int MASKFF = 0xff;  //ffh as mask 
   
    // System Commands
    public final static int LEADINGX = 0x58;
    public final static int XSTATUS = 0xA2;
    public final static int XEVENT = 0xC8;
    public final static int XSENSOR = 0x98;
    public final static int XSENSOFF = 0x99;
    public final static int XEVTSEN = 0xCB;
    public final static int XEVTLOK = 0xC9;
    public final static int XEVTTRN = 0xCA;
    public final static int XPWROFF = 0xA6;
    public final static int XPWRON = 0xA7;
}

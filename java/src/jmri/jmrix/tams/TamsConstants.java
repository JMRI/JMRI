package jmri.jmrix.tams;

/**
 * Constants to represent values seen in Tams traffic
 *
 * @author  Jan Boen 151216
 * @author Based on work from Kevin Dickerson
 */
public final class TamsConstants {

    // Various bit masks
    public static final int XPWRMASK = 0x08;  //bit 3 of XStatus 
    public static final int XSENMASK = 0x04;  //bit 2 of XEvent
    public static final int XLOKMASK = 0x01;  //bit 0 of XEvent
    public static final int XTRNMASK = 0x18;  //bits 4 and 5 of XEvent

    // Various other elements
    public static final int EOM80 = 0x80;  //80h as end of message
    public static final int EOM00 = 0x00;  //00h as end of message 
    public static final int POLLMSG = 0x00;  //first byte for a Poll related TamsMessage 
    public static final int MASKFF = 0xff;  //ffh as mask 
   
    // System Commands
    public static final int LEADINGX = 0x58;
    public static final int XSTATUS = 0xA2;
    public static final int XEVENT = 0xC8;
    public static final int XSENSOR = 0x98;
    public static final int XSENSOFF = 0x99;
    public static final int XEVTSEN = 0xCB;
    public static final int XEVTLOK = 0xC9;
    public static final int XEVTTRN = 0xCA;
    public static final int XPWROFF = 0xA6;
    public static final int XPWRON = 0xA7;
}

// X10.java

package jmri.jmrix.powerline;


/**
 * Constants for X10.
 * 
 * These might someday have to be device specific, unfortunately.
 *
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision: 1.1 $
 */
public class X10 {

    public static final int POLL_REQ        = 0x5a;
    public static final int TIME_REQ        = 0xa5;
    public static final int MACRO_INITIATED = 0x5b;
    public static final int CHECKSUM_OK     = 0x00;
    public static final int READY_REQ       = 0x55;

    public static final int POLL_ACK        = 0xc3;
    public static final int TIMER_DOWNLOAD  = 0x9b;

    public static final int FUNCTION_ALL_UNITS_OFF          = 0;
    public static final int FUNCTION_ALL_LIGHTS_ON          = 1;
    public static final int FUNCTION_ON                     = 2;
    public static final int FUNCTION_OFF                    = 3;
    public static final int X10_FUNCTION_DIM                    = 4;
    public static final int X10_FUNCTION_BRIGHT                 = 5;
    public static final int X10_FUNCTION_ALL_LIGHTS_OFF         = 6;
    public static final int X10_FUNCTION_EXTENDED_CODE          = 7;
    public static final int X10_FUNCTION_HAIL_REQUEST           = 8;
    public static final int X10_FUNCTION_HAIL_ACKNOWLEDGE       = 9;
    public static final int X10_FUNCTION_PRESET_DIM_1           = 10;
    public static final int X10_FUNCTION_PRESET_DIM_2           = 11;
    public static final int X10_FUNCTION_EXTENDED_DATA_TRANSFER = 12;
    public static final int X10_FUNCTION_STATUS_ON              = 13;
    public static final int X10_FUNCTION_STATUS_OFF             = 14;
    public static final int X10_FUNCTION_STATUS_REQUEST         = 15;

    /**
     * For the house (A-P) and device (1-16) codes, get
     * the decimal value.
     * Argument is from 1 to 16 only.
     */
    public static int encode(int i) {
        if (i<1 || i>16) throw new IllegalArgumentException("Encode outside 1-16: "+i);
        return encoder[i];
    }
    static final int[] encoder = new int[]{-1, 0x6, 0xE, 0x2, 0xA, 0x1, 0x9, 0x5,
                                            0xD, 0x7, 0xF, 0x3, 0xB, 0x0, 0x8, 0x4, 0xC};
}


/* @(#)X10.java */

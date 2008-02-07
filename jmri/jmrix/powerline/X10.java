// X10.java

package jmri.jmrix.powerline;


/**
 * Constants for X10.
 * 
 * These might someday have to be device specific, unfortunately.
 *
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision: 1.3 $
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
    public static final int FUNCTION_DIM                    = 4;
    public static final int FUNCTION_BRIGHT                 = 5;
    public static final int FUNCTION_ALL_LIGHTS_OFF         = 6;
    public static final int FUNCTION_EXTENDED_CODE          = 7;
    public static final int FUNCTION_HAIL_REQUEST           = 8;
    public static final int FUNCTION_HAIL_ACKNOWLEDGE       = 9;
    public static final int FUNCTION_PRESET_DIM_1           = 10;
    public static final int FUNCTION_PRESET_DIM_2           = 11;
    public static final int FUNCTION_EXTENDED_DATA_TRANSFER = 12;
    public static final int FUNCTION_STATUS_ON              = 13;
    public static final int FUNCTION_STATUS_OFF             = 14;
    public static final int FUNCTION_STATUS_REQUEST         = 15;

    static String[] functionNames = new String[]{
        "All Off", "All Lights On", "On", "Off",
        "Dim", "Bright", "All Lights Off", "Extended Code",
        "Hail Request", "Hail Ack", "Preset Dim 1", "Preset Dim 2",
        "Ext Data Trnsfr", "Status On", "Status Off", "Status Req"
    };
    
    /**
     * Return a readable name for a function code
     */
    public static String functionName(int i) {
        return functionNames[i];
    }
    
    /**
     * For the house (A-P) and device (1-16) codes, get
     * the line-coded value.
     * Argument is from 1 to 16 only.
     */
    public static int encode(int i) {
        if (i<1 || i>16) throw new IllegalArgumentException("Encode outside 1-16: "+i);
        return encoder[i];
    }
    static final int[] encoder = new int[]{-1, 
                                    0x6, 0xE, 0x2, 0xA,   0x1, 0x9, 0x5, 0xD, // 1-8
                                    0x7, 0xF, 0x3, 0xB,   0x0, 0x8, 0x4, 0xC};

    /**
     * Get house (A-P) or device (1-16) from line-coded
     * value.
     */
    public static int decode(int i) {
        if (i<0 || i>15) throw new IllegalArgumentException("Decode outside 1-16: "+i);
        return decoder[i];
    }
    static final int[] decoder = new int[]{13, 
                                    5, 3, 11, 15,   7, 1, 9, 14, // 1-8
                                    6, 4, 12, 16,   8, 2, 10}; // 9-15

    /**
     * Pretty-print an address code
     */
    public static String formatAddressByte(int b) {
        return "House "+X10.decode((b>>4)&0x0F)
            +" address device "+X10.decode(b&0x0f);
    }

    /**
     * Pretty-print a function code
     */
    public static String formatCommandByte(int b) {
        return "House "+X10.decode((b>>4)&0x0F)
                +" function: "+X10.functionName(b&0x0f);
    }
    
}


/* @(#)X10.java */

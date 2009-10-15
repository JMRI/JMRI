// Constants.java

package jmri.jmrix.powerline.insteon2412s;


/**
 * Constants and functions specific to the Insteon 2412S interface
 *
 * @author			Bob Jacobsen Copyright (C) 2008, 2009
 * @version			$Revision: 1.1 $
 */
public class Constants {

    public static final int POLL_REQ        = 0x5a;
    public static final int TIME_REQ        = 0xa5;
    public static final int MACRO_INITIATED = 0x5b;
    public static final int CHECKSUM_OK     = 0x00;
    public static final int READY_REQ       = 0x55;

    public static final int POLL_ACK        = 0xc3;
    public static final int TIMER_DOWNLOAD  = 0x9b;


    /**
     * Pretty-print a header code
     */
    public static String formatHeaderByte(int b) {
        return "Dim: " + ((b >> 3)& 0x1F)
                + ((b & 0x02) != 0 ? " function" : " address " )
                + ((b & 0x01) != 0 ? " extended" : " ");
    }
    
}


/* @(#)Constants.java */

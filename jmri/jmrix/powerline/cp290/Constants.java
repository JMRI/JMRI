// Constants.java

package jmri.jmrix.powerline.cp290;


/**
 * Constants and functions specific to the CM11 interface
 *
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision: 1.2 $
 */
public class Constants {



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

//NceEpromChecker.java

package jmri.jmrix.nce;

import javax.swing.JOptionPane;

/* 
 * Checks revision of NCE CS by reading the 3 byte revision.
 * Sends a warning message NCE EPROM found & preferences
 * are not correct for revsion selected. 
 * 
 * Also checks for March 2007 EPROM and warns user about Monitoring feedback.
 *  
 * @author Daniel Boudreau (C) 2007
 * @version     $Revision: 1.3 $
 * 
 */

public class NceEpromChecker implements NceListener{
	
	public static final int SW_REV_CMD = 0xAA;	// NCE get EPROM revision cmd, Reply Format: VV.MM.mm
	private static final int REPLY_LEN = 3;		// number of bytes read
	
	public static boolean nceEpromMarch2007 = false;	// flag to allow JMRI to be bug for bug compatible
	public static boolean nceUSBdetected = false;		// true if NCE USB was detected 
	
	// Our current knowledge of NCE Command Station EPROMs
	private static final int VV_1999 = 4;		// Revision of Apr 1999 EPROM VV.MM.mm = 4.0.1
	private static final int MM_1999 = 0;
	private static final int mm_1999 = 1;
	
	private static final int VV_2004 = 6;		// Revision of Dec 2004 EPROM VV.MM.mm = 6.0.0
	private static final int MM_2004 = 0;
	private static final int mm_2004 = 0;
	
	private static final int VV_2007 = 6;		// Revision of Mar 2007 EPROM VV.MM.mm = 6.2.0
	private static final int MM_2007 = 2;
	private static final int mm_2007 = 0;
	
	private static final int mm_2007a = 1;		// Revision of May 2007 EPROM VV.MM.mm = 6.2.1

	// USB -> Cab bus adapter:
	// When used with PowerCab V1.28 - 6.3.0
	// When used with SB3 V1.28 - 6.3.1 (No program track on an SB3)
	// When used with PH-Pro or PH-10 - 6.3.2 (limited set of features available
	// through cab bus)
	//
	// NOTE: The USB port can not read CS memory 	
	private static final int VV_USB = 6;		// Revision of USB EPROM VV.MM.mm = 6.3.x
	private static final int MM_USB = 3;
	private static final int mm_USB_PwrCab = 0;		// PowerCab
	private static final int mm_USB_SB3 = 1;		// SB3
	private static final int mm_USB_PH = 2;			// PH-Pro or PH-10

	
	public NceMessage NceEpromPoll() {
		
        byte [] bl = new byte [1];
        bl [0] = (byte) (SW_REV_CMD);
        NceMessage m = NceMessage.createBinaryMessage(bl, REPLY_LEN);
        return m;

	}
	
    public void message(NceMessage m){
        if (log.isDebugEnabled()) {
            log.debug("unexpected message" );
        }	
    }
    
    public void reply(NceReply r) {
        if (r.getNumDataElements()== REPLY_LEN) {
        	
        	byte VV = (byte)r.getElement(0);
        	byte MM = (byte)r.getElement(1);
        	byte mm = (byte)r.getElement(2);
        	
        	// Send to log file the NCE EPROM revision
         	log.info ("NCE EPROM revision = " 
        			+ Integer.toHexString(VV & 0xFF)+"."
    				+ Integer.toHexString(MM & 0xFF)+"."
    				+ Integer.toHexString(mm & 0xFF));
        	
        	// Confirm that user selected correct revision of EPROM, check for old EPROM installed, new EPROM preferences
         	if ((VV <= VV_2007 && MM < MM_2007) && (NceMessage.getCommandOptions() >= NceMessage.OPTION_2006)){
        		log.error("Wrong revision (" 
        				+ Integer.toHexString(VV & 0xFF)+"."
        				+ Integer.toHexString(MM & 0xFF)+"."
        				+ Integer.toHexString(mm & 0xFF)+") of the NCE Command Station EPROM selected in Preferences");
        		JOptionPane.showMessageDialog(null, "Wrong revision of Command Station EPROM selected in Preferences",
        				"Error", JOptionPane.ERROR_MESSAGE);
        	}
         	
         	// Confirm that user selected correct revision of EPROM, check for new EPROM installed, old EPROM preferences
         	boolean eprom2007orNewer = false;
         	if ((VV == VV_2007)&&(MM >= MM_2007)) eprom2007orNewer = true;
        	if (((VV > VV_2007)|| eprom2007orNewer  ) & (NceMessage.getCommandOptions() < NceMessage.OPTION_2006)){
        		log.error("Wrong revision (" 
        				+ Integer.toHexString(VV & 0xFF)+"."
        				+ Integer.toHexString(MM & 0xFF)+"."
        				+ Integer.toHexString(mm & 0xFF)+") of the NCE Command Station EPROM selected in Preferences");
        		JOptionPane.showMessageDialog(null, "Wrong revision of Command Station EPROM selected in Preferences",
        				"Error", JOptionPane.ERROR_MESSAGE);
        	}
         	
        	// Warn about the March 2007 CS EPROM	
         	if (VV == VV_2007 && MM == MM_2007 && mm == mm_2007 ){
        		nceEpromMarch2007 = true;
        		log.warn("This revision ("
        				+ Integer.toHexString(VV & 0xFF)+"."
        				+ Integer.toHexString(MM & 0xFF)+"."
        				+ Integer.toHexString(mm & 0xFF)+") of the NCE Command Station EPROM has problems with MONITORING feedback");
 // Need to add checkbox "Do not show this message again" otherwise the message can be a pain. 
 //       		JOptionPane.showMessageDialog(null, "The 2007 March EPROM doesn't provide reliable feedback," +
 //       				" contact NCE if you want to use MONITORING feedback  ",
 //       				"Warning", JOptionPane.INFORMATION_MESSAGE);
        	}
         	// Check for USB
         	if (VV == VV_USB && MM == MM_USB ){
         		nceUSBdetected = true;
         	}

        }
        else log.warn("wrong number of read bytes for revision check");
    }    
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceEpromChecker.class.getName());	
    
}
/* @(#)NceEpromChecker.java */



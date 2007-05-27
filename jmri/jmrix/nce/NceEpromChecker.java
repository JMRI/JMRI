//NceEpromChecker.java

package jmri.jmrix.nce;

import javax.swing.JOptionPane;

/* 
 * Checks revision of NCE CS by reading the 3 byte revision
 * Sends a warning message if pre 2006 EPROM found
 * 
 * Also checks for March 2007 EPROM
 *  
 * @author Daniel Boudreau (C) 2007
 * @version     $Revision: 1.2 $
 * 
 */

public class NceEpromChecker implements NceListener{
	
	public static final int SW_REV_CMD = 0xAA;	// NCE get EPROM revision cmd, Reply Format: VV.MM.mm
	private static final int REPLY_LEN = 3;		// number of bytes read
	
	public static boolean nceEpromMarch2007 = false;
	
	// Our current knowledge of NCE EPROMs	
	private static final int VV_2004 = 6;		// Revision of 2004 EPROM VV.MM.mm = 6.0.0
	private static final int MM_2004 = 0;
	private static final int mm_2004 = 0;
	private static final int VV_2007 = 6;		// Revision of March 2007 EPROM VV.MM.mm = 6.2.0
	private static final int MM_2007 = 2;
	private static final int mm_2007 = 0;
	
	
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
        	
//       	 Confirm that user selected correct version of EPROM
            
        	if (MM < MM_2007 & VV <= VV_2007){
        		log.error("Wrong version of Command Station EPROM selected in Preferences: "
        				+ Integer.toHexString(VV & 0xFF)+"."
        				+ Integer.toHexString(MM & 0xFF)+"."
        				+ Integer.toHexString(mm & 0xFF));
                JOptionPane.showMessageDialog(null, "Wrong version of Command Station EPROM selected in Preferences",
                        "Error", JOptionPane.ERROR_MESSAGE);
        	}
        	if (VV == VV_2007 && MM == MM_2007 && mm == mm_2007 ){
        		nceEpromMarch2007 = true;
        		log.warn("This version of Command Station EPROM has problems with MONITORING feedback: "
        				+ Integer.toHexString(VV & 0xFF)+"."
        				+ Integer.toHexString(MM & 0xFF)+"."
        				+ Integer.toHexString(mm & 0xFF));
        		JOptionPane.showMessageDialog(null, "The 2007 March EPROM doesn't provide reliable feedback," +
        				" contact NCE if you want to use MONITORING feedback  ",
                        "Warning", JOptionPane.INFORMATION_MESSAGE);
        	}
        	
        }
        else log.warn("wrong number of read bytes for revision check");
    }    
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceEpromChecker.class.getName());	
    
}
/* @(#)NceEpromChecker.java */



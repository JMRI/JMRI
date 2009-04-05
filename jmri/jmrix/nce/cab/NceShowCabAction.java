/** 
 * NceShowCabAction.java
 *
 * Description:		Swing action to create and register a 
 *       			NceShowCabFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @author			Daniel Boudreau Copyright (C) 2007
 * @version			
 */

package jmri.jmrix.nce.cab;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import jmri.jmrix.nce.NceUSB;

public class NceShowCabAction  extends AbstractAction {

	public NceShowCabAction(String s) {
		super(s);

		// disable if NCE USB detected
		if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE) {
			setEnabled(false);
		}
	}
	
    public void actionPerformed(ActionEvent e) {
		NceShowCabFrame f = new NceShowCabFrame();
		f.addHelpMenu("package.jmri.jmrix.nce.cab.NceShowCabFrame", true);
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);	
	}
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NceShowCabAction.class.getName());
}


/* @(#)NceShowCabAction.java */


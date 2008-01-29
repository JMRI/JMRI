/** 
 * NceConsistEditAction.java
 *
 * Description:		Swing action to create and register a 
 *       			NceConsistEditFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @author			Daniel Boudreau Copyright (C) 2007
 * @version			
 */

package jmri.jmrix.nce.consist;
 

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import jmri.jmrix.nce.NceUSB;

public class NceConsistEditAction  extends AbstractAction {

	public NceConsistEditAction(String s) { 
		super(s);
		
		// disable if NCE USB selected
		if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE) {
			setEnabled(false);
		}
	}
	
    public void actionPerformed(ActionEvent e) {
		NceConsistEditFrame f = new NceConsistEditFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);	
	}
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceConsistEditAction.class.getName());
}


/* @(#)NceConsistEditAction.java */


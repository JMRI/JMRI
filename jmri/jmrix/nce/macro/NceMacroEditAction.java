/** 
 * NceMacroEditAction.java
 *
 * Description:		Swing action to create and register a 
 *       			NceMacroEditFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @author			Daniel Boudreau Copyright (C) 2007
 * @version			
 */

package jmri.jmrix.nce.macro;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import jmri.jmrix.nce.NceEpromChecker;

public class NceMacroEditAction  extends AbstractAction {

	public NceMacroEditAction(String s) {
		super(s);

		// disable if NCE USB detected
		if (NceEpromChecker.nceUSBdetected) {
			setEnabled(false);
		}
	}
	
    public void actionPerformed(ActionEvent e) {
		NceMacroEditFrame f = new NceMacroEditFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);	
	}
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceMacroEditAction.class.getName());
}


/* @(#)NceMacroEditAction.java */


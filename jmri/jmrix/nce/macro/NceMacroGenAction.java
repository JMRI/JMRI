/** 
 * NceMacroGenAction.java
 *
 * Description:		Swing action to create and register a 
 *       			NceMacroGenFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @author			Daniel Boudreau Copyright (C) 2007
 * @version			
 */

package jmri.jmrix.nce.macro;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class NceMacroGenAction  extends AbstractAction {

	public NceMacroGenAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		NceMacroGenFrame f = new NceMacroGenFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);	
	}
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceMacroGenAction.class.getName());
}


/* @(#)NceMacroGenAction.java */


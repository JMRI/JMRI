/** 
 * SimpleProgAction.java
 *
 * Description:		Swing action to create and register a 
 *       			SimpleProgAction object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package jmri.simpleprog;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import ErrLoggerJ.ErrLog;
import jmri.jmrix.loconet.SlotManager;

public class SimpleProgAction 			extends AbstractAction {

	public SimpleProgAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {

		// create SlotManager if it doesn't exist
		SlotManager.instance();

		// create a SimpleProgFrame
		SimpleProgFrame f = new SimpleProgFrame();
		f.show();	
		
	}
}


/* @(#)SimpleProgAction.java */

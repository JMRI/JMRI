/** 
 * SlotMonAction.java
 *
 * Description:		Swing action to create and register a 
 *       			LocoEchoFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package simpleprog;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import ErrLoggerJ.ErrLog;
import loconet.SlotManager;

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

/** 
 * SlotMonAction.java
 *
 * Description:		Swing action to create and register a 
 *       			LocoEchoFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package slotmon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import ErrLoggerJ.ErrLog;
import loconet.SlotManager;

public class SlotMonAction 			extends AbstractAction {

	public SlotMonAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {

		// create SlotManager if it doesn't exist
		SlotManager.instance();

		// create a SlotMonFrame
		SlotMonFrame f = new SlotMonFrame();
		try {
			//f.initComponents();
			}
		catch (Exception ex) {
			ErrLog.msg(ErrLog.error, "SlotMonAction starting SlotMonoFrame:", "", "Exception: "+ex.toString());
			}
		f.show();	
		
	}
}


/* @(#)SlotMonAction.java */

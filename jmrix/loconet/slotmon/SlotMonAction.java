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
		// create a SlotMonFrame
		SlotMonFrame f = new SlotMonFrame();
		try {
			//f.initComponents();
			}
		catch (Exception ex) {
			ErrLog.msg(ErrLog.error, "SlotMonAction starting SlotMonoFrame:", "", "Exception: "+ex.toString());
			}
		f.show();	
		
		// create SlotManager if it doesn't exist
		if (SlotManager.instance() == null) {
			SlotManager temp = new SlotManager();
			}
		// connect to the SlotManager to be notified of each slot change
		SlotManager.instance().addSlotListener(f);	
	}
}


/* @(#)SlotMonAction.java */

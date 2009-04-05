/**
 * EliteAction.java
 *
 * Description:		Swing action to create and register a
 *       		frame for serial XPressNet access via the 
 *                      Hornby Elite's built in USB port. 
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @author			Paul Bender Copyright (C) 2008
 * @version			$Revision: 1.2 $
 */

package jmri.jmrix.lenz.hornbyelite;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class EliteAction 			extends AbstractAction {

	public EliteAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a EliteFrame
		EliteFrame f = new EliteFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting Elite frame caught exception: "+ex.toString());
			}
		f.setVisible(true);
	};

   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EliteAction.class.getName());

}


/* @(#)EliteAction.java */

/**
 * ZTC640Action.java
 *
 * Description:		Swing action to create and register a
 *       			frame for serial XPressNet access via a ZTC640
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */

package jmri.jmrix.lenz.ztc640;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ZTC640Action 			extends AbstractAction {

	public ZTC640Action(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a ZTC640Frame
		ZTC640Frame f = new ZTC640Frame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting ZTC640 frame caught exception: "+ex.toString());
			}
		f.setVisible(true);
	};

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ZTC640Action.class.getName());

}


/* @(#)ZTC640Action.java */

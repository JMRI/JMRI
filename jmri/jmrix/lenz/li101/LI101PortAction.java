/**
 * LI101PortAction.java
 *
 * Description:		Swing action to create and register a
 *       			frame for serial XPressNet access via a LI101
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 2.1 $
 */

package jmri.jmrix.lenz.li101;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class LI101PortAction 			extends AbstractAction {

	public LI101PortAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a LI101Frame
		LI101PortFrame f = new LI101PortFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting LI101 frame caught exception: "+ex.toString());
			}
		f.show();
	};

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LI101Action.class.getName());

}


/* @(#)LI101Action.java */

/**
 * LI100Action.java
 *
 * Description:		Swing action to create and register a
 *       			frame for serial XPressNet access via a LI100
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.1 $
 */

package jmri.jmrix.lenz.li100;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class LI100Action 			extends AbstractAction {

	public LI100Action(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a LI100Frame
		LI100Frame f = new LI100Frame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting LI100 frame caught exception: "+ex.toString());
			}
		f.show();
	};

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LI100Action.class.getName());

}


/* @(#)LI100Action.java */

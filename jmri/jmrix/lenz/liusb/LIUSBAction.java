/**
 * LIUSBAction.java
 *
 * Description:		Swing action to create and register a
 *       			frame for serial XPressNet access via a LIUSB
 *
 * @author			Paul Bender    Copyright (C) 2005
 * @version			$Revision: 1.2 $
 */

package jmri.jmrix.lenz.liusb;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class LIUSBAction 			extends AbstractAction {

	public LIUSBAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a LIUSBFrame
		LIUSBFrame f = new LIUSBFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting LIUSB frame caught exception: "+ex.toString());
			}
		f.setVisible(true);
	};

   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LIUSBAction.class.getName());

}


/* @(#)LIUSBAction.java */

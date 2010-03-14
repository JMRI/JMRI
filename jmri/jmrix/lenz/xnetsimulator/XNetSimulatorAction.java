/**
 * XNetSimulatorAction.java
 *
 * Description:		Swing action to create and register a
 *       			frame for serial XPressNet access via a XNetSimulator
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @author			Paul Bender Copyright (C) 2009
 * @version			$Revision: 1.4 $
 * @deprecated 2.9.5
 */

package jmri.jmrix.lenz.xnetsimulator;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

@Deprecated
public class XNetSimulatorAction 			extends AbstractAction {

	public XNetSimulatorAction(String s) { super(s);}

    public String getUserName() { return "XPressNet"; }

    public void actionPerformed(ActionEvent e) {
		// create a XNetSimulatorFrame
		XNetSimulatorFrame f = new XNetSimulatorFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting XNetSimulator frame caught exception: "+ex.toString());
			}
		f.setVisible(true);
	}

   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetSimulatorAction.class.getName());

}


/* @(#)XNetSimulatorAction.java */

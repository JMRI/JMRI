/**
 * XnTcpAction.java
 *
 * Description:		Swing action to create and register a
 *       			frame for serial XPressNet access via a XnTcp
 *
 * @author			Giorgio Terdina Copyright (C) 2008, based on LI100 Action by Bob Jacobsen, Copyright (C) 2001
 * @version			$Revision: 1.1 $
 */

package jmri.jmrix.lenz.xntcp;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class XnTcpAction 			extends AbstractAction {

	public XnTcpAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a XnTcpFrame
		XnTcpFrame f = new XnTcpFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting XnTcp frame caught exception: "+ex.toString());
			}
		f.setVisible(true);
	};

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XnTcpAction.class.getName());

}


/* @(#)XnTcpAction.java */

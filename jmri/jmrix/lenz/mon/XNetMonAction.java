/**
 * XNetMonAction.java
 *
 * Description:		Swing action to create and register a
 *       			XNetMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2002
 * @version         $Revision: 2.0 $
 */

package jmri.jmrix.lenz.mon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.lenz.XNetTrafficController;


public class XNetMonAction 			extends AbstractAction {

	public XNetMonAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a XNetMonFrame
		XNetMonFrame f = new XNetMonFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("XNetMonAction starting XNetMonFrame: Exception: "+ex.toString());
			}
		f.show();

	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetMonAction.class.getName());

}


/* @(#)XNetMonAction.java */

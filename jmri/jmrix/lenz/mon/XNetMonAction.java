/**
 * XNetMonAction.java
 *
 * Description:		Swing action to create and register a
 *       			XNetMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2002
 * @author			Paul Bender     Copyright (C) 2008
 * @version         $Revision: 2.3 $
 */

package jmri.jmrix.lenz.mon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;


public class XNetMonAction extends AbstractAction {

    public XNetMonAction(String s) { super(s);}

    public XNetMonAction(){
	this("XPressNet Monitor");
    }

    public void actionPerformed(ActionEvent e) {
		// create a XNetMonFrame
		XNetMonFrame f = new XNetMonFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("XNetMonAction starting XNetMonFrame: Exception: "+ex.toString());
			}
		f.setVisible(true);

	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetMonAction.class.getName());

}


/* @(#)XNetMonAction.java */

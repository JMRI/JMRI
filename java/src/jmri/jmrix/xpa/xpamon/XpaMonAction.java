//XpaMonAction.java

package jmri.jmrix.xpa.xpamon;

import org.apache.log4j.Logger;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 *       			xpaMonFrame object
 *
 * @author			Paul Bender    Copyright (C) 2004
 * @version			$Revision$
 */

public class XpaMonAction 			extends AbstractAction {

	public XpaMonAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a XpaMonFrame
		XpaMonFrame f = new XpaMonFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("XpaMonAction starting XpaMonFrame: Exception: "+ex.toString());
			}
		f.setVisible(true);
	}

	static Logger log = Logger.getLogger(XpaMonAction.class.getName());

}


/* @(#)XpaMonAction.java */

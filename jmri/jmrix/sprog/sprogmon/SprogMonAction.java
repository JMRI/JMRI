//SprogMonAction.java

package jmri.jmrix.sprog.sprogmon;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 *       			SprogMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */

public class SprogMonAction 			extends AbstractAction {

	public SprogMonAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a SprogMonFrame
		SprogMonFrame f = new SprogMonFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("SprogMonAction starting SprogMonFrame: Exception: "+ex.toString());
			}
		f.show();
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogMonAction.class.getName());

}


/* @(#)SprogMonAction.java */

//SprogMonAction.java

package jmri.jmrix.sprog.sprogmon;

import org.apache.log4j.Logger;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 *       			SprogMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision$
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
		f.setVisible(true);
	}

	static Logger log = Logger.getLogger(SprogMonAction.class.getName());

}


/* @(#)SprogMonAction.java */

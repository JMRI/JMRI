//SprogMonAction.java

package jmri.jmrix.sprog.sprogmon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	static Logger log = LoggerFactory.getLogger(SprogMonAction.class.getName());

}


/* @(#)SprogMonAction.java */

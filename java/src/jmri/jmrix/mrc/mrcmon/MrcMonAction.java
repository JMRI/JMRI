// MrcMonAction.java

package jmri.jmrix.mrc.mrcmon;

import org.apache.log4j.Logger;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


/**
 * Swing action to create and register a
 *       			MrcMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision$
 */
public class MrcMonAction 			extends AbstractAction {

	public MrcMonAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a MrcMonFrame
		MrcMonFrame f = new MrcMonFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("MrcMonAction starting MrcMonFrame: Exception: "+ex.toString());
			}
		f.setVisible(true);
	}

	static Logger log = Logger.getLogger(MrcMonAction.class.getName());

}


/* @(#)MrcMonAction.java */

// MrcMonAction.java

package jmri.jmrix.mrc.mrcmon;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


/**
 * Swing action to create and register a
 *       			MrcMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.1 $
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
		f.show();
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MrcMonAction.class.getName());

}


/* @(#)MrcMonAction.java */

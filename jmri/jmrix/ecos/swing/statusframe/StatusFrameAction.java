// StatusFrameAction.java

package jmri.jmrix.ecos.swing.statusframe;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a 
 *       			StatusFrame object
 *
 * @author	    Bob Jacobsen    Copyright (C) 2008
 * @version		$Revision: 1.1 $	
 */

public class StatusFrameAction extends AbstractAction {

	public StatusFrameAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		StatusFrame f = new StatusFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			ex.printStackTrace();
			}
		f.setVisible(true);	
	}
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(StatusFrameAction.class.getName());
}


/* @(#)StatusFrameAction.java */

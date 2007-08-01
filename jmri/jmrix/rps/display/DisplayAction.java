// DisplayAction.java

package jmri.jmrix.rps.display;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			DisplayFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2006
 * @version         $Revision: 1.1 $
 */
public class DisplayAction 			extends AbstractAction {

	public DisplayAction(String s) { super(s);}

    public DisplayAction() {
        this("RPS Calculations Display");
    }

    public void actionPerformed(ActionEvent e) {
        log.debug("starting frame creation");
		DisplayFrame f = new DisplayFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("starting frame: Exception: "+ex.toString());
			}
		f.setVisible(true);

	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DisplayAction.class.getName());

}


/* @(#)DisplayAction.java */

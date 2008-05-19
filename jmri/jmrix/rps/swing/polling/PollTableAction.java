// PollTableAction.java

package jmri.jmrix.rps.swing.polling;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			PollTableFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2008
 * @version         $Revision: 1.1 $
 */
public class PollTableAction 			extends AbstractAction {

	public PollTableAction(String s) { super(s);}

    public PollTableAction() {
        this("RPS Polling Control");
    }

    public void actionPerformed(ActionEvent e) {
        log.debug("starting frame creation");
		PollTableFrame f = new PollTableFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("starting frame: Exception: "+ex.toString());
			}
		f.setVisible(true);

	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PollTableAction.class.getName());

}


/* @(#)PollTableAction.java */

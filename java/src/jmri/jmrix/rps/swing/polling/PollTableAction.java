// PollTableAction.java

package jmri.jmrix.rps.swing.polling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			PollTableFrame object. 
 * <p>
 * We only permit one, because notification is not entirely right yet.
 *
 * @author			Bob Jacobsen    Copyright (C) 2008
 * @version         $Revision$
 */
public class PollTableAction 			extends AbstractAction {

	public PollTableAction(String s) { super(s);}

    public PollTableAction() {
        this("RPS Polling Control");
    }

    PollTableFrame f = null;
    
    public void actionPerformed(ActionEvent e) {
        log.debug("starting frame creation");
        if (f==null) {
            f = new PollTableFrame();
            try {
                f.initComponents();
                }
            catch (Exception ex) {
                log.warn("starting frame: Exception: "+ex.toString());
                }
        }
		f.setVisible(true);

	}

	static Logger log = LoggerFactory.getLogger(PollTableAction.class.getName());

}


/* @(#)PollTableAction.java */

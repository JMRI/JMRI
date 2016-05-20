// DebuggerAction.java

package jmri.jmrix.rps.swing.debugger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			DisplayFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2008
 * @version         $Revision$
 */
public class DebuggerAction 			extends AbstractAction {

	public DebuggerAction(String s) { super(s);}

    public DebuggerAction() {
        this("RPS Debugger Window");
    }

    public void actionPerformed(ActionEvent e) {
        log.debug("starting frame creation");
		DebuggerFrame f = new DebuggerFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("starting frame: Exception: "+ex.toString());
			}
		f.setVisible(true);

	}

	static Logger log = LoggerFactory.getLogger(DebuggerAction.class.getName());

}


/* @(#)DebuggerAction.java */

// DebuggerAction.java

package jmri.jmrix.rps.swing.debugger;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			DisplayFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2008
 * @version         $Revision: 1.1 $
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

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DebuggerAction.class.getName());

}


/* @(#)DebuggerAction.java */

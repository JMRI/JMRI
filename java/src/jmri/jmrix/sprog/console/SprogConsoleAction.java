// SprogConsoleAction.java

package jmri.jmrix.sprog.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a SprogConsoleFrame object
 *
 * @author			Andrew Crosland    Copyright (C) 2008
 * @version			$Revision$
 */
public class SprogConsoleAction extends AbstractAction {

	public SprogConsoleAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		SprogConsoleFrame f = new SprogConsoleFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);
	}
   static Logger log = LoggerFactory.getLogger(SprogConsoleAction.class.getName());
}


/* @(#)SprogConsoleAction.java */

// SpeedoConsoleAction.java

package jmri.jmrix.bachrus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a SpeedoConsoleFrame object
 *
 * @author			Andrew Crosland    Copyright (C) 2010
 * @version			$Revision$
 */
public class SpeedoConsoleAction extends AbstractAction {

	public SpeedoConsoleAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		SpeedoConsoleFrame f = new SpeedoConsoleFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);
	}
   static Logger log = LoggerFactory.getLogger(SpeedoConsoleAction.class.getName());
}


/* @(#)SpeedoConsoleAction.java */

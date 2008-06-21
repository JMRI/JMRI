// CbusConsoleAction.java

package jmri.jmrix.can.cbus.swing.console;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a CbusConsoleFrame object
 *
 * @author			Andrew Crosland    Copyright (C) 2008
 * @version			$Revision: 1.1 $
 */
public class CbusConsoleAction extends AbstractAction {

	public CbusConsoleAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		CbusConsoleFrame f = new CbusConsoleFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);
	}
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CbusConsoleAction.class.getName());
}


/* @(#)CbusConsoleAction.java */

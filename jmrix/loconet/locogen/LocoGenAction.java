/** 
 * LocoGenAction.java
 *
 * Description:		Swing action to create and register a 
 *       			Loco(Mon)GenFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet.locogen;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import ErrLoggerJ.ErrLog;

import jmri.jmrix.loconet.LnTrafficController;

public class LocoGenAction 			extends AbstractAction {

	public LocoGenAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		// create a LocoGenFrame
		LocoGenFrame f = new LocoGenFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			ErrLog.msg(ErrLog.error, "LocoGenAction starting LocoGenFrame:", "", "Exception: "+ex.toString());
			}
		f.show();	
		
		// connect to the LnTrafficController
		f.connect(LnTrafficController.instance());
	}
}


/* @(#)LocoGenAction.java */

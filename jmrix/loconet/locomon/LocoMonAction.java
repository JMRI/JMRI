/** 
 * LocoMonAction.java
 *
 * Description:		Swing action to create and register a 
 *       			LocoMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet.locomon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import ErrLoggerJ.ErrLog;

import jmri.jmrix.loconet.LnTrafficController;


public class LocoMonAction 			extends AbstractAction {

	public LocoMonAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		// create a LocoMonFrame
		LocoMonFrame f = new LocoMonFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			ErrLog.msg(ErrLog.error, "LocoMonAction starting LocoMonFrame:", "", "Exception: "+ex.toString());
			}
		f.show();	
		
		// connect to the LnTrafficController
		LnTrafficController.instance().addLocoNetListener(~0, f);		
	}
}


/* @(#)LocoMonAction.java */

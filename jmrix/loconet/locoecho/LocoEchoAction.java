/** 
 * LocoEchoAction.java
 *
 * Description:		Swing action to create and register a 
 *       			LocoEchoFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet.locoecho;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import ErrLoggerJ.ErrLog;

import jmri.jmrix.loconet.LnTrafficController;

public class LocoEchoAction 			extends AbstractAction {

	public LocoEchoAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		// create a LocoEchoFrame
		LocoEchoFrame f = new LocoEchoFrame();
		try {
			//f.initComponents();
			}
		catch (Exception ex) {
			ErrLog.msg(ErrLog.error, "LocoEchoAction starting LocoEchoFrame:", "", "Exception: "+ex.toString());
			}
		f.show();	
		
		// connect to the LnTrafficController
		LnTrafficController.instance().addLocoNetListener(~0, f);	
	}
}


/* @(#)LocoEchoAction.java */

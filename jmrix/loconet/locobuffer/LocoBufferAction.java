/** 
 * LocoBufferAction.java
 *
 * Description:		Swing action to create and register a 
 *       			LocoBufferFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet.locobuffer;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import ErrLoggerJ.ErrLog;

public class LocoBufferAction 			extends AbstractAction {

	public LocoBufferAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		// create a LocoBufferFrame
		LocoBufferFrame f = new LocoBufferFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			ErrLog.msg(ErrLog.error, "LocoBufferAction","starting LocoBufferFrame:", "Exception: "+ex.toString());
			}
		f.show();			
	};

}


/* @(#)LnHexFileAction.java */

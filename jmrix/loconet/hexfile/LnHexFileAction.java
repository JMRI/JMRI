/** 
 * LnHexFileAction.java
 *
 * Description:		Swing action to create and register a 
 *       			LnHexFileFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet.hexfile;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import jmri.jmrix.loconet.hexfile.HexFileFrame;
import ErrLoggerJ.ErrLog;

public class LnHexFileAction 			extends AbstractAction {

	public LnHexFileAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		// create a LnHexFileFrame
		HexFileFrame f = new HexFileFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			ErrLog.msg(ErrLog.error, "LocoMonAppl.HexFileAction starting HexFileFrame:", "", "Exception: "+ex.toString());
			}
		f.show();	
		// it connects to the LnTrafficController when the right button is pressed
		
		
	};

}


/* @(#)LnHexFileAction.java */

/** 
 * SymbolicProgAction.java
 *
 * Description:		Swing action to create and register a 
 *       			SymbolicProg object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package jmri.symbolicprog;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import ErrLoggerJ.ErrLog;

public class SymbolicProgAction 			extends AbstractAction {

	public SymbolicProgAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {

		// create a SimpleProgFrame
		SymbolicProgFrame f = new SymbolicProgFrame();
		f.show();	
		
	}
}


/* @(#)SymbolicProgAction.java */

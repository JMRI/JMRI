/** 
 * SymbolicProgAction.java
 *
 * Description:		Swing action to create and register a 
 *       			SymbolicProg object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: SymbolicProgAction.java,v 1.2 2002-01-08 04:09:27 jacobsen Exp $
 */

package jmri.jmrit.symbolicprog.symbolicframe;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class SymbolicProgAction 			extends AbstractAction {

	public SymbolicProgAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {

		// create a SimpleProgFrame
		SymbolicProgFrame f = new SymbolicProgFrame();
		f.show();	
		
	}
}


/* @(#)SymbolicProgAction.java */

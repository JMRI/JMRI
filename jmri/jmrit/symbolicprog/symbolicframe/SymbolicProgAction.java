/** 
 * SymbolicProgAction.java
 *
 * Description:		Swing action to create and register a 
 *       			SymbolicProg object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: SymbolicProgAction.java,v 1.1 2002-02-28 20:29:39 jacobsen Exp $
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

/**
 * SymbolicProgAction.java
 *
 * Description:		Swing action to create and register a
 *       			SymbolicProg object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision$
 */

package jmri.jmrit.symbolicprog.symbolicframe;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class SymbolicProgAction 			extends AbstractAction {

	public SymbolicProgAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {

		// create a SimpleProgFrame
		SymbolicProgFrame f = new SymbolicProgFrame();
		f.setVisible(true);

	}
}


/* @(#)SymbolicProgAction.java */

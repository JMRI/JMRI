// LocoIOAction.java

package jmri.jmrix.loconet.locoio;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


/**
 * LocoIOAction.java
 *
 * Description:		Swing action to create and register a
 *       			LocoIOFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2002
 * @version			$Revision: 1.3 $
 */
public class LocoIOAction 			extends AbstractAction {

	public LocoIOAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a LocoMonFrame
		LocoIOFrame f = new LocoIOFrame();
		f.setVisible(true);
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoIOAction.class.getName());

}


/* @(#)LocoIOAction.java */

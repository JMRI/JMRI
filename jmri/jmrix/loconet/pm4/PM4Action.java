// PM4Action.java

package jmri.jmrix.loconet.pm4;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.loconet.LnTrafficController;


/**
 * PM4Action.java
 *
 * Description:		Swing action to create and register a
 *       			PM4Frame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2002
 * @version			$Revision: 1.2 $
 */
public class PM4Action 			extends AbstractAction {

    public PM4Action(String s) { super(s);}

    public PM4Action() { this("PM4 programmer");}

    public void actionPerformed(ActionEvent e) {
		// create a PM4Frame
		PM4Frame f = new PM4Frame();
		f.show();
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PM4Action.class.getName());

}


/* @(#)PM4Action.java */

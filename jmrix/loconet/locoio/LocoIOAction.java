// LocoIOAction.java

package jmri.jmrix.loconet.locoio;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.loconet.LnTrafficController;


/** 
 * LocoIOAction.java
 *
 * Description:		Swing action to create and register a 
 *       			LocoIOFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2002
 * @version			$Id: LocoIOAction.java,v 1.1 2002-02-20 16:00:42 jacobsen Exp $
 */
public class LocoIOAction 			extends AbstractAction {

	public LocoIOAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		// create a LocoMonFrame
		LocoIOFrame f = new LocoIOFrame();
		f.show();			
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoIOAction.class.getName());

}


/* @(#)LocoIOAction.java */

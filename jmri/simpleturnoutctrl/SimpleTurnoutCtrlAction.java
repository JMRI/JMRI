/** 
 * LocoEchoAction.java
 *
 * Description:		Swing action to create and register a 
 *       			LocoEchoFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package jmri.simpleturnoutctrl;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.loconet.LnTrafficController;

public class SimpleTurnoutCtrlAction 			extends AbstractAction {

	public SimpleTurnoutCtrlAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		// create a LocoEchoFrame
		SimpleTurnoutCtrlFrame f = new SimpleTurnoutCtrlFrame();
		try {
			//f.initComponents();
			}
		catch (Exception ex) {
			log.error("exception during construction: "+ex.toString());
			}
		f.show();	
		
	}
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SimpleTurnoutCtrlAction.class.getName());
}


/* @(#)SimpleTurnoutCtrlAction.java */

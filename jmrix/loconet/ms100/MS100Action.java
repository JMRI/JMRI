/** 
 * MS100Action.java
 *
 * Description:		Swing action to create and register a 
 *       			MS100Frame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet.ms100;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import ErrLoggerJ.ErrLog;

public class MS100Action 			extends AbstractAction {

	public MS100Action(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		// create a MS100Frame
		MS100Frame f = new MS100Frame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting MS100Frame caught exception: "+ex.toString());
			}
		f.show();			
	};

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MS100Action.class.getName());

}


/* @(#)LnHexFileAction.java */

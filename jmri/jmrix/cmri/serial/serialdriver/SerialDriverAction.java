/** 
 * SerialDriverAction.java
 *
 * Description:		Swing action to create and register a 
 *       			cmri.serial.SerialDriverFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2002
 * @version			$Id: SerialDriverAction.java,v 1.1 2002-03-03 05:50:45 jacobsen Exp $
 */

package jmri.jmrix.cmri.serial.serialdriver;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class SerialDriverAction 			extends AbstractAction {

	public SerialDriverAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		SerialDriverFrame f = new SerialDriverFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting cmri.serial.SerialDriverFrame caught exception: "+ex.toString());
			}
		f.show();			
	};
 
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialDriverAction.class.getName());

}


/* @(#)SerialDriverAction.java */

/** 
 * SerialDriverAction.java
 *
 * Description:		Swing action to create and register a 
 *       			SerialDriverFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.nce.serialdriver;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class SerialDriverAction extends AbstractAction  {

	public SerialDriverAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		// create a SerialDriverFrame
		SerialDriverFrame f = new SerialDriverFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting SerialDriverFrame caught exception: "+ex.toString());
			}
		f.show();			
	};

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialDriverAction.class.getName());

}


/* @(#)LnHexFileAction.java */

/**
 * LIUSBServerAction.java
 *
 * Description:		Swing action to create and register a
 *       		frame for serial XPressNet access via The Lenz 
 *       		LIUSB Server 
 *
 * @author	        Paul Bender Copyright (C) 2009
 * @version			$Revision: 1.3 $
 * @deprecated 2.9.5
 */

package jmri.jmrix.lenz.liusbserver;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

@Deprecated
public class LIUSBServerAction extends AbstractAction {

    public LIUSBServerAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a LIUSBServerFrame
		LIUSBServerFrame f = new LIUSBServerFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting LIUSBServer frame caught exception: "+ex.toString());
			}
		//f.setVisible(true);
	}
static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LIUSBServerAction.class.getName());

}


/* @(#)LIUSBServerAction.java */

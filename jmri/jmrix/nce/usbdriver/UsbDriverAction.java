// SerialDriverAction.java

package jmri.jmrix.nce.usbdriver;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			SerialDriverFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.1 $
 */
public class UsbDriverAction extends AbstractAction  {

	public UsbDriverAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a SerialDriverFrame
		UsbDriverFrame f = new UsbDriverFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting SerialDriverFrame caught exception: "+ex.toString());
			}
		f.setVisible(true);
	};

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(UsbDriverAction.class.getName());

}


/* @(#)SerialDriverAction.java */

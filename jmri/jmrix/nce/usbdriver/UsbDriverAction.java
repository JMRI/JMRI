// UsbDriverAction.java

package jmri.jmrix.nce.usbdriver;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a UsbDriverFrame object
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @version $Revision: 1.5 $
 */
@Deprecated
public class UsbDriverAction extends AbstractAction  {

	public UsbDriverAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a UsbDriverFrame
		UsbDriverFrame f = new UsbDriverFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting UsbDriverFrame caught exception: "+ex.toString());
			}
		f.setVisible(true);
	}

   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UsbDriverAction.class.getName());

}


/* @(#)UsbDriverAction.java */

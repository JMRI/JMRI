/** 
 * SerialDriverFrame.java
 *
 * Description:		Frame to control and connect Sprog command station via SerialDriver interface and comm port
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: SerialDriverFrame.java,v 1.1 2003-01-27 05:35:40 jacobsen Exp $
 */

package jmri.jmrix.sprog.serialdriver;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.jmrix.sprog.SprogTrafficController;

public class SerialDriverFrame extends jmri.jmrix.SerialPortFrame {

	public SerialDriverFrame() {
		super("Open Sprog connection");
		adapter = new SerialDriverAdapter();
	}

	public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
		if ((String) portBox.getSelectedItem() != null) {
			// connect to the port
			adapter.openPort((String) portBox.getSelectedItem(),"SerialDriverFrame");
				
			adapter.configure();
						
			// hide this frame, since we're done
			hide();
		} else {
			// not selected
			JOptionPane.showMessageDialog(this, "Please select a port name first");
		}
	}

}

// SerialDriverFrame.java

package jmri.jmrix.xpa.serialdriver;

import javax.swing.JOptionPane;

/**
 * Frame to control and connect Xpa+Modem connected to an XPressNet Based 
 * command station via SerialDriver interface and comm port
 * @author			Paul Bender   Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */
public class SerialDriverFrame extends jmri.jmrix.SerialPortFrame {

	public SerialDriverFrame() {
		super("Open Xpa connection");
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

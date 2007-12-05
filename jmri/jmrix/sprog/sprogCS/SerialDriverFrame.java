// SerialDriverFrame.java

package jmri.jmrix.sprog.sprogCS;

import javax.swing.JOptionPane;

/**
 * Frame to control and connect Sprog command station via SerialDriver interface and comm port
 * @author			Andrew Crosland   Copyright (C) 2006
 * @version			$Revision: 1.1 $
 */
public class SerialDriverFrame extends jmri.jmrix.SerialPortFrame {

	public SerialDriverFrame() {
		super("Open Sprog Command Station connection");
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

/* @(#)SerialdriverFrame.java */


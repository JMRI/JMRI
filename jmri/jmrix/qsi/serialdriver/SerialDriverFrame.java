// SerialDriverFrame.java

package jmri.jmrix.qsi.serialdriver;

import javax.swing.JOptionPane;

/**
 * Frame to control and connect QSI command station via SerialDriver interface and comm port
 * @author			Bob Jacobsen   Copyright (C) 2007
 * @version			$Revision: 1.1 $
 */
public class SerialDriverFrame extends jmri.jmrix.SerialPortFrame {

	public SerialDriverFrame() {
		super("Open QSI connection");
		adapter = new SerialDriverAdapter();
	}

	public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
		if ((String) portBox.getSelectedItem() != null) {
			// connect to the port
			adapter.openPort((String) portBox.getSelectedItem(),"SerialDriverFrame");

			adapter.configure();

			// hide this frame, since we're done
      setVisible(false);
		} else {
			// not selected
			JOptionPane.showMessageDialog(this, "Please select a port name first");
		}
	}

}

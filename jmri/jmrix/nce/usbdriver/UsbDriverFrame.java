/** 
 * UsbDriverFrame.java
 *
 * Description:		Frame to control and connect NCE command station via SerialDriver interface and comm port
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.nce.usbdriver;

import javax.swing.*;
@Deprecated 
public class UsbDriverFrame extends jmri.jmrix.SerialPortFrame {

	public UsbDriverFrame() {
		super("Open NCE connection");
		adapter = new UsbDriverAdapter();
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

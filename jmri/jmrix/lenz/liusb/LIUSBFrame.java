// LIUSBFrame.java

package jmri.jmrix.lenz.liusb;

import javax.swing.JOptionPane;

/**
 * Frame to control and connect XPressNet via LIUSB interface
 * @author			Paul Bender   Copyright (C) 2005
 * @version			$Revision: 1.0 $
 */
public class LIUSBFrame extends jmri.jmrix.SerialPortFrame {

	public LIUSBFrame() {
		super("Open LIUSB");
		adapter = new LIUSBAdapter();
	}

	public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
		if ((String) portBox.getSelectedItem() != null) {
			// connect to the port
			adapter.configureBaudRate((String)baudBox.getSelectedItem());
			adapter.configureOption1((String)opt1Box.getSelectedItem());
			adapter.configureOption2((String)opt2Box.getSelectedItem());
			String errCode = adapter.openPort((String) portBox.getSelectedItem(),"LIUSBFrame");

			if (errCode == null)	{
				adapter.configure();
				// check for port in OK state
				if (!((LIUSBAdapter)adapter).okToSend()) {
					log.info("LIUSB port not ready to send");
					JOptionPane.showMessageDialog(null,
				   		"The LIUSB is unable to accept data.\n"
				   		+"Make sure its power is on, it is connected\n"
				   		+"to a working XPressNet, and the command station is on.\n"
				   		+"Reset the LIUSB by cycling its power.\n"
				   		+"Then restart this program.",
				   		"LIUSB not ready", JOptionPane.ERROR_MESSAGE);
				}
				// hide this frame, since we're done
				hide();
			} else {
				JOptionPane.showMessageDialog(this,errCode);
			}
		} else {
			// not selected
			JOptionPane.showMessageDialog(this, "Please select a port name first");
		}
	}

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LIUSBFrame.class.getName());

}

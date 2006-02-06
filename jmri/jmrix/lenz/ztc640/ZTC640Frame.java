// ZTC640Frame.java

package jmri.jmrix.lenz.ztc640;

import javax.swing.JOptionPane;

/**
 * Frame to control and connect XPressNet via ZTC640 interface
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version			$Revision: 1.1 $
 */
public class ZTC640Frame extends jmri.jmrix.SerialPortFrame {

	public ZTC640Frame() {
		super("Open ZTC640");
		adapter = new ZTC640Adapter();
	}

	public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
		if ((String) portBox.getSelectedItem() != null) {
			// connect to the port
			adapter.configureBaudRate((String)baudBox.getSelectedItem());
			adapter.configureOption1((String)opt1Box.getSelectedItem());
			adapter.configureOption2((String)opt2Box.getSelectedItem());
			String errCode = adapter.openPort((String) portBox.getSelectedItem(),"ZTC640Frame");

			if (errCode == null)	{
				adapter.configure();
				// check for port in OK state
				if (!((ZTC640Adapter)adapter).okToSend()) {
					log.info("ZTC640 port not ready to send");
					JOptionPane.showMessageDialog(null,
				   		"The ZTC640 is unable to accept data.\n"
				   		+"Make sure its power is on, it is connected\n"
				   		+"to a working XPressNet, and the command station is on.\n"
				   		+"Reset the ZTC640 by cycling its power.\n"
				   		+"Then restart this program.",
				   		"ZTC640 not ready", JOptionPane.ERROR_MESSAGE);
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

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ZTC640Frame.class.getName());

}

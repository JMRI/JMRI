// LI101PortFrame.java

package jmri.jmrix.lenz.li101;

import javax.swing.JOptionPane;

/**
 * Frame to control and connect XPressNet via LI101 interface and comm port
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version			$Revision: 2.1 $
 */
public class LI101PortFrame extends jmri.jmrix.SerialPortFrame {

	public LI101PortFrame() {
		super("Open LI101");
		adapter = new LI101Adapter();
	}

	public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
		if ((String) portBox.getSelectedItem() != null) {
			// connect to the port
			adapter.configureBaudRate((String)baudBox.getSelectedItem());
			adapter.configureOption1((String)opt1Box.getSelectedItem());
			adapter.configureOption2((String)opt2Box.getSelectedItem());
			String errCode = adapter.openPort((String) portBox.getSelectedItem(),"LI101PortFrame");

			if (errCode == null)	{
				adapter.configure();
				// check for port in OK state
				if (!((LI101Adapter)adapter).okToSend()) {
					log.info("LI101 port not ready to send");
					JOptionPane.showMessageDialog(null,
				   		"The LI101 is unable to accept data.\n"
				   		+"Make sure its power is on, it is connected\n"
				   		+"to a working XPressNet, and the command station is on.\n"
				   		+"Reset the LI101 by cycling its power.\n"
				   		+"Then restart this program.",
				   		"LI101 not ready", JOptionPane.ERROR_MESSAGE);
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

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LI101Frame.class.getName());

}

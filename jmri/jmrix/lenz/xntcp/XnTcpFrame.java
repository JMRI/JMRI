// XnTcpFrame.java

package jmri.jmrix.lenz.xntcp;

import javax.swing.JOptionPane;

/**
 * Frame to control and connect XPressNet via XnTcp interface and comm port
 * @author			Giorgio Terdina Copyright (C) 2008, based on LI100 Frame by Bob Jacobsen, Copyright (C) 2002
 * @version			$Revision: 1.4 $
 */
public class XnTcpFrame extends jmri.jmrix.NetworkPortFrame {

        javax.swing.JComboBox portBox = new javax.swing.JComboBox();
 
	public XnTcpFrame() {
		super("Open XnTcp");
		adapter = new XnTcpAdapter();
	}

	public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.NetworkConfigException {
		if ((String) portBox.getSelectedItem() != null) {
			// connect to the port
			adapter.configureOption1((String)opt1Box.getSelectedItem());
			adapter.configureOption2((String)opt2Box.getSelectedItem());
			String errCode = ((XnTcpAdapter)adapter).openPort((String) portBox.getSelectedItem(),"XnTcpFrame");

			if (errCode == null)	{
				adapter.configure();
				// check for port in OK state
				if (!((XnTcpAdapter)adapter).okToSend()) {
					log.info("XnTcp port not ready to send");
					JOptionPane.showMessageDialog(null,
				   		"The XnTcp is unable to accept data.\n"
				   		+"Make sure its power is on, it is connected\n"
				   		+"to a working XPressNet, and the command station is on.\n"
				   		+"Reset the XnTcp by cycling its power.\n"
				   		+"Then restart this program.",
				   		"XnTcp not ready", JOptionPane.ERROR_MESSAGE);
				}
				// hide this frame, since we're done
        setVisible(false);
			} else {
				JOptionPane.showMessageDialog(this,errCode);
			}
		} else {
			// not selected
			JOptionPane.showMessageDialog(this, "Please select a port name first");
		}
	}

   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XnTcpFrame.class.getName());

}

// EliteFrame.java

package jmri.jmrix.lenz.hornbyelite;

import javax.swing.JOptionPane;

/**
 * Frame to control and connect XPressNet via Elite interface and comm port
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @author			Paul Bender    Copyright (C) 2003,2008
 * @version			$Revision: 1.1 $
 */
public class EliteFrame extends jmri.jmrix.SerialPortFrame {

	public EliteFrame() {
		super("Open Elite");
		adapter = new EliteAdapter();
	}

	public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
		if ((String) portBox.getSelectedItem() != null) {
			// connect to the port
			adapter.configureBaudRate((String)baudBox.getSelectedItem());
			adapter.configureOption1((String)opt1Box.getSelectedItem());
			adapter.configureOption2((String)opt2Box.getSelectedItem());
			String errCode = adapter.openPort((String) portBox.getSelectedItem(),"EliteFrame");

			if (errCode == null)	{
				adapter.configure();
				// check for port in OK state
				if (!((EliteAdapter)adapter).okToSend()) {
					log.info("Elite port not ready to send");
					JOptionPane.showMessageDialog(null,
				   		"The Elite is unable to accept data.\n"
				   	       +"Make sure its power is on, and the\n"
                                               +"USB cable is connected both the Elite\n"
                                                +"and the computer.\n"
				   		+"Reset the Elite by cycling its power.\n"
				   		+"Then restart this program.",
				   		"Elite not ready", JOptionPane.ERROR_MESSAGE);
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

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EliteFrame.class.getName());

}

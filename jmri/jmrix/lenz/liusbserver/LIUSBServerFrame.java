// LIUSBServerFrame.java

package jmri.jmrix.lenz.liusbserver;

import javax.swing.JOptionPane;

/**
 * Frame to control and connect XPressNet via the LIUSB Server interface
 * @author			Paul Bender (C) 2009
 * @version			$Revision: 1.2 $
 */
public class LIUSBServerFrame extends jmri.jmrix.NetworkPortFrame {

        private LIUSBServerAdapter adapter = null;

	public LIUSBServerFrame() {
		super("Open LIUSB Server");
		adapter = new LIUSBServerAdapter();
			String errCode = adapter.openPort(hostField.getText(),"LIUSBServerFrame");
			if (errCode == null)	{
				adapter.configure();
                }
                setVisible(false);
	}

	public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.NetworkConfigException{
                hostField.setText("localhost"); // the LIUSB server only
                                                // works with localhost.
		if (hostField.getText() != null) {
			// connect to the host 

			String errCode = adapter.openPort(hostField.getText(),"LIUSBServerFrame");
			if (errCode == null)	{
				adapter.configure();
				// check for port in OK state
				if (!(adapter).okToSend()) {
					log.info("LIUSBServer port not ready to send");
					JOptionPane.showMessageDialog(null,
				   		"The LIUSBServer is unable to accept data.\n"
				   		+"Make sure its power is on, it is connected\n"
				   		+"to a working XPressNet, and the command station is on.\n"
				   		+"Reset the LIUSBServer by cycling its power.\n"
				   		+"Then restart this program.",
				   		"LIUSBServer not ready", JOptionPane.ERROR_MESSAGE);
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
static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LIUSBServerFrame.class.getName());

}

/** 
 * LocoBufferFrame.java
 *
 * Description:		Frame to control and connect LocoNet via LocoBuffer interface and comm port
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet.locobuffer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;

import jmri.jmrix.loconet.LnTrafficController;

public class LocoBufferFrame extends jmri.jmrix.SerialPortFrame {

	public LocoBufferFrame() {
		super("Open LocoBuffer");
		adapter = new LocoBufferAdapter();
	}

	public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
		if ((String) portBox.getSelectedItem() != null) {
			// connect to the port
			adapter.configureBaudRate((String)baudBox.getSelectedItem());
			adapter.configureOption1((String)opt1Box.getSelectedItem());
			String errCode = adapter.openPort((String) portBox.getSelectedItem(),"LocoBufferFrame");
			
			if (errCode == null)	{
				adapter.configure();						
				// check for port in OK state
				if (!((LocoBufferAdapter)adapter).okToSend()) {
					log.info("LocoBuffer port not ready to send");
					JOptionPane.showMessageDialog(null, 
				   		"The LocoBuffer is unable to accept data.\n"
				   		+"Make sure its power is on, it is connected\n"
				   		+"to a working LocoNet, and the command station is on.\n"
				   		+"The LocoNet LED on the LocoBuffer should be off.\n"
				   		+"Reset the LocoBuffer by cycling its power.\n"
				   		+"Then restart this program.", 
				   		"LocoBuffer not ready", JOptionPane.ERROR_MESSAGE);
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
	
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoBufferFrame.class.getName());

}

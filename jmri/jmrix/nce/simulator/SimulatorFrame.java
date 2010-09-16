/** 
 * SerialDriverFrame.java
 *
 * Description:		Frame to control and connect NCE command station via SerialDriver interface and comm port
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.nce.simulator;

@Deprecated 
public class SimulatorFrame extends jmri.jmrix.SerialPortFrame {

	public SimulatorFrame() {
		super("Open NCE connection");
		adapter = new SimulatorAdapter();
	}

	public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
			// make the serial stream connections
			adapter.openPort((String) portBox.getSelectedItem(),"SimulatorFrame");				
			adapter.configure();					
			// hide this frame, since we're done
			setVisible(false);
	}

}

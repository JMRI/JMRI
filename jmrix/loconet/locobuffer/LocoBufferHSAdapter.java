/** 
 * LocoBufferAdapter.java
 *
 * Title:			LocoBufferHSAdapter
 * Description:		Provide access to LocoNet via a LocoBuffer attached to a serial comm port
 *					at 57600 baud.
 *					Normally controlled by the LocoBufferFrame class.
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: LocoBufferHSAdapter.java,v 1.2 2002-02-20 15:57:03 jacobsen Exp $
 */

package jmri.jmrix.loconet.locobuffer;

import javax.comm.SerialPort;

public class LocoBufferHSAdapter extends LocoBufferAdapter {

	protected void setSerialPort() throws javax.comm.UnsupportedCommOperationException {
		// force baud rate, configure comm options
		int baud = 57600;  
		activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			
		// set RTS high, DTR high - done early, so flow control can be configured after
		activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
		activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

		// find and configure flow control
		int flow = SerialPort.FLOWCONTROL_RTSCTS_OUT; // default, but also deftauls in selectedOption1
		if (selectedOption1.equals(validOption1[1]))
			flow = 0;
		activeSerialPort.setFlowControlMode(flow);
	}			

}

/** 
 * LocoBufferAdapter.java
 *
 * Title:			LocoBufferHSAdapter
 * Description:		Provide access to LocoNet via a LocoBuffer attached to a serial comm port
 *					at 57600 baud.
 *					Normally controlled by the LocoBufferFrame class.
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: LocoBufferHSAdapter.java,v 1.1 2002-01-16 07:37:28 jacobsen Exp $
 */

package jmri.jmrix.loconet.locobuffer;

import javax.comm.SerialPort;

public class LocoBufferHSAdapter extends LocoBufferAdapter {

	protected void setSerialPort() throws javax.comm.UnsupportedCommOperationException {
				activeSerialPort.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
	}			

}

/** 
 * SerialPortController.java
 *
 * Description:		Abstract base for classes representing a CMRI communications port
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: SerialPortController.java,v 1.1 2002-03-03 05:50:45 jacobsen Exp $
 */

package jmri.jmrix.cmri.serial;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class SerialPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to SerialTrafficController classes, who in turn will deal in messages.
	
	// returns the InputStream from the port
	public abstract DataInputStream getInputStream();
	
	// returns the outputStream to the port
	public abstract DataOutputStream getOutputStream();
	
	// check that this object is ready to operate
	public abstract boolean status();
}


/* @(#)SerialPortController.java */

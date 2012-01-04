// GcPortController.java

package jmri.jmrix.can.adapters.gridconnect;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a GridConnect communications port
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @author                      Andrew Crosland 2008
 * @version			$Revision$
 */public abstract class GcPortController extends jmri.jmrix.AbstractSerialPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to CabrsTrafficController classes, who in turn will deal in messages.

	// returns the InputStream from the port
	public abstract DataInputStream getInputStream();

	// returns the outputStream to the port
	public abstract DataOutputStream getOutputStream();

	// check that this object is ready to operate
	public abstract boolean status();
    
    protected jmri.jmrix.can.CanSystemConnectionMemo adaptermemo;
    
}


/* @(#)GcPortController.java */

// MrcPortController.java

package jmri.jmrix.mrc;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a MRC communications port
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision$
 */
public abstract class MrcPortController extends jmri.jmrix.AbstractSerialPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to MrcTrafficController classes, who in turn will deal in messages.

	// returns the InputStream from the port
	abstract public DataInputStream getInputStream();

	// returns the outputStream to the port
	abstract public DataOutputStream getOutputStream();

	// check that this object is ready to operate
	abstract public boolean status();
}


/* @(#)MrcPortController.java */

// XpaPortController.java

package jmri.jmrix.xpa;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing an XPA+Modem communications port
 * @author			Paul Bender    Copyright (C) 2004
 * @version			$Revision$
 */public abstract class XpaPortController extends jmri.jmrix.AbstractSerialPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to XpaTrafficController classes, who in turn will deal in messages.

	// returns the InputStream from the port
	public abstract DataInputStream getInputStream();

	// returns the outputStream to the port
	public abstract DataOutputStream getOutputStream();

	// check that this object is ready to operate
	public abstract boolean status();
}


/* @(#)XpaPortController.java */

// SRCPPortController.java

package jmri.jmrix.srcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a SRCP communications port
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @version			$Revision: 1.1 $
 */
public abstract class SRCPPortController extends jmri.jmrix.AbstractPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to SRCPTrafficController classes, who in turn will deal in messages.

	// returns the InputStream from the port
	abstract public DataInputStream getInputStream();

	// returns the outputStream to the port
	abstract public DataOutputStream getOutputStream();

	// check that this object is ready to operate
	abstract public boolean status();
}


/* @(#)SRCPPortController.java */

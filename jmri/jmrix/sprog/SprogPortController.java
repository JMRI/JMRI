/**
 * SprogPortController.java
 *
 * Description:		Abstract base for classes representing a Sprog communications port
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: SprogPortController.java,v 1.1 2003-01-27 05:24:00 jacobsen Exp $
 */

package jmri.jmrix.sprog;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class SprogPortController extends jmri.jmrix.AbstractPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to SprogTrafficController classes, who in turn will deal in messages.

	// returns the InputStream from the port
	public abstract DataInputStream getInputStream();

	// returns the outputStream to the port
	public abstract DataOutputStream getOutputStream();

	// check that this object is ready to operate
	public abstract boolean status();
}


/* @(#)SprogPortController.java */

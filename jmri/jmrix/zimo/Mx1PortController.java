/**
 * Mx1PortController.java
 *
 * Description:		Abstract base for classes representing a MX-1 communications port
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.1 $
 *
 * Adapted by Sip Bosch for use with zimo Mx-1
 */

package jmri.jmrix.zimo;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class Mx1PortController extends jmri.jmrix.AbstractPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to Mx1TrafficController classes, who in turn will deal in messages.

	// returns the InputStream from the port
	public abstract DataInputStream getInputStream();

	// returns the outputStream to the port
	public abstract DataOutputStream getOutputStream();

	/**
	 * Check that this object is ready to operate. This is a question
	 * of configuration, not transient hardware status.
	 */
	public abstract boolean status();

	/**
	 * Can the port accept additional characters?  This might
	 * go false for short intervals, but it might also stick
	 * off if something goes wrong.
	 */
	public abstract boolean okToSend();
}


/* @(#)Mx1PortController.java */

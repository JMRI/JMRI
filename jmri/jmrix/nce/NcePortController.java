/** 
 * NcePortController.java
 *
 * Description:		Abstract base for classes representing a NCE communications port
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.nce;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class NcePortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to NceTrafficController classes, who in turn will deal in messages.
	
	// returns the InputStream from the port
	public abstract DataInputStream getInputStream();
	
	// returns the outputStream to the port
	public abstract DataOutputStream getOutputStream();
	
	// check that this object is ready to operate
	public abstract boolean status();
}


/* @(#)NcePortController.java */

/** 
 * LnPortController.java
 *
 * Description:		Abstract base for classes representing a LocoNet communications port
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package loconet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class LnPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to LnTrafficController classes, who in turn will deal in messages.
	
	// returns the InputStream from the port
	public abstract DataInputStream getInputStream();
	
	// returns the outputStream to the port
	public abstract DataOutputStream getOutputStream();
	
	// check that this object is ready to operate
	public abstract boolean status();
}


/* @(#)LnPortController.java */

/** 
 * EasyDccPortController.java
 *
 * Description:		Abstract base for classes representing a EasyDcc communications port
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: EasyDccPortController.java,v 1.1 2002-03-23 07:28:30 jacobsen Exp $
 */

package jmri.jmrix.easydcc;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class EasyDccPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to EasyDccTrafficController classes, who in turn will deal in messages.
	
	// returns the InputStream from the port
	public abstract DataInputStream getInputStream();
	
	// returns the outputStream to the port
	public abstract DataOutputStream getOutputStream();
	
	// check that this object is ready to operate
	public abstract boolean status();
}


/* @(#)EasyDccPortController.java */

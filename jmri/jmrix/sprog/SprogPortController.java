// SprogPortController.java

package jmri.jmrix.sprog;

/*
 * Identifying class representing a ECOS communications port
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @version $Revision: 1.4 $
 */

public abstract class SprogPortController extends jmri.jmrix.AbstractSerialPortController {

	// base class. Implementations will provide InputStream and OutputStream
	// objects to SprogTrafficController classes, who in turn will deal in messages.
    protected SprogSystemConnectionMemo adaptermemo = null;
}


/* @(#)SprogPortController.java */

// PortController.java

package jmri.jmrix.direct;

/*
 * Identifying class representing a direct-drive communications port
 * @author			Bob Jacobsen    Copyright (C) 2001, 2004
 * @version $Revision: 1.1 $
 */

public abstract class PortController extends jmri.jmrix.AbstractPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to TrafficController classes, who in turn will deal in messages.

}


/* @(#)TPortController.java */

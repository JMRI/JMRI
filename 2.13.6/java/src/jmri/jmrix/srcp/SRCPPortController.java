// SRCPPortController.java

package jmri.jmrix.srcp;

/**
 * Abstract base for classes representing a SRCP communications port
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008, 2010
 * @author			Paul Bender     Copyright (C) 2010
 * @version			$Revision$
 */
public abstract class SRCPPortController extends jmri.jmrix.AbstractNetworkPortController {

	// base class. Implementations will provide InputStream and OutputStream
	// objects to SRCPTrafficController classes, who in turn will deal in messages.
    protected SRCPSystemConnectionMemo adaptermemo = null;
}


/* @(#)SRCPPortController.java */

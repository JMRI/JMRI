// SRCPPortController.java

package jmri.jmrix.srcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a SRCP communications port
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @author			Paul Bender     Copyright (C) 2010
 * @version			$Revision: 1.2 $
 */
public abstract class SRCPPortController extends jmri.jmrix.NetworkPortAdapter {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to SRCPTrafficController classes, who in turn will deal in messages.

}


/* @(#)SRCPPortController.java */

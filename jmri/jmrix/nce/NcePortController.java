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

public abstract class NcePortController extends jmri.jmrix.AbstractPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to NceTrafficController classes, who in turn will deal in messages.

}


/* @(#)NcePortController.java */

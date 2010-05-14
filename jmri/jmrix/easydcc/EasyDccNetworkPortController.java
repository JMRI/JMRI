// EasyDccNetworkPortController.java

package jmri.jmrix.easydcc;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a EasyDcc communications port
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.1 $
 */
public abstract class EasyDccNetworkPortController extends jmri.jmrix.AbstractNetworkPortController implements jmri.jmrix.NetworkPortAdapter{
	// base class. Implementations will provide InputStream and OutputStream
	// objects to EasyDccTrafficController classes, who in turn will deal in messages.

    protected EasyDccSystemConnectionMemo adaptermemo = null;
}


/* @(#)EasyDccPortController.java */

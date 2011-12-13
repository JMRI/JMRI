// EasyDccPortController.java

package jmri.jmrix.easydcc;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a EasyDcc communications port
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision$
 */
public abstract class EasyDccPortController extends jmri.jmrix.AbstractSerialPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to EasyDccTrafficController classes, who in turn will deal in messages.

	// returns the InputStream from the port
	abstract public DataInputStream getInputStream();

	// returns the outputStream to the port
	abstract public DataOutputStream getOutputStream();

	// check that this object is ready to operate
	abstract public boolean status();

    protected EasyDccSystemConnectionMemo adaptermemo = null;
    
    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }
}


/* @(#)EasyDccPortController.java */

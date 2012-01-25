// QsiPortController.java

package jmri.jmrix.qsi;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a QSI communications port
 * @author			Bob Jacobsen    Copyright (C) 2007
 * @version			$Revision$
 */public abstract class QsiPortController extends jmri.jmrix.AbstractSerialPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to QsiTrafficController classes, who in turn will deal in messages.

	// returns the InputStream from the port
	public abstract DataInputStream getInputStream();

	// returns the outputStream to the port
	public abstract DataOutputStream getOutputStream();

	// check that this object is ready to operate
	public abstract boolean status();
    
    protected QsiSystemConnectionMemo adaptermemo = null;
    
    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }
}


/* @(#)QsiPortController.java */

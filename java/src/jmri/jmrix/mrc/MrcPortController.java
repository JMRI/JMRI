// MrcPortController.java

package jmri.jmrix.mrc;

import jmri.jmrix.SystemConnectionMemo;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a MRC communications port
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @author			Kevin Dickerson    Copyright (C) 2014
 * @version			$Revision$
 */
public abstract class MrcPortController extends jmri.jmrix.AbstractSerialPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to MrcTrafficController classes, who in turn will deal in messages.

	// returns the InputStream from the port
	/*abstract public DataInputStream getInputStream();

	// returns the outputStream to the port
	abstract public DataOutputStream getOutputStream();*/

	// check that this object is ready to operate
	abstract public boolean status();
    
    protected MrcSystemConnectionMemo adaptermemo = null;
    
    // returns the InputStream from the port
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    public abstract DataOutputStream getOutputStream();
    
    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }
    
    public boolean okToSend() {
        return true;
    }
    
    @Override
    public SystemConnectionMemo getSystemConnectionMemo() { 
    	if (adaptermemo == null)
    		adaptermemo= new MrcSystemConnectionMemo();
    	return adaptermemo; 
    }
}


/* @(#)MrcPortController.java */

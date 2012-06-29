// TamsPortController.java

package jmri.jmrix.tams;

/**
 * Identifying class representing a Tams communications port
 * Based on work by Bob Jacobsen
 * @author	Kevin Dickerson  Copyright (C) 2012
 * @version $Revision: 17977 $
 */

public abstract class TamsPortController extends jmri.jmrix.AbstractSerialPortController {

	// base class. Implementations will provide InputStream and OutputStream
	// objects to TamsTrafficController classes, who in turn will deal in messages.
    protected TamsSystemConnectionMemo adaptermemo = null;
    
    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }
}


/* @(#)TamsPortController.java */

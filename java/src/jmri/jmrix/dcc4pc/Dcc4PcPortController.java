// Dcc4PcPortController.java

package jmri.jmrix.dcc4pc;

/*
 * Identifying class representing a DCC4PC communications port
 * @author          Kevin Dickerson Copyright (C) 2012
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @version $Revision: 18133 $
 */

public abstract class Dcc4PcPortController extends jmri.jmrix.AbstractSerialPortController {

	// base class. Implementations will provide InputStream and OutputStream
	// objects to Dcc4PcTrafficController classes, who in turn will deal in messages.
    protected Dcc4PcSystemConnectionMemo adaptermemo = null;
    
    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }
}


/* @(#)Dcc4PcPortController.java */

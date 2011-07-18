// NceNetworkPortController.java

package jmri.jmrix.nce;

import jmri.jmrix.SystemConnectionMemo;

/*
 * Identifying class representing a NCE communications port
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version $Revision$
 */

public abstract class NceNetworkPortController extends jmri.jmrix.AbstractNetworkPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to NceTrafficController classes, who in turn will deal in messages.
    protected NceSystemConnectionMemo adaptermemo = null;
    
    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }
    
    @Override
    public SystemConnectionMemo getSystemConnectionMemo() { 
    	if (adaptermemo == null)
    		adaptermemo = new NceSystemConnectionMemo();
    	return adaptermemo; 
    }
}


/* @(#)NceNetworkPortController.java */

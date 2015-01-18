// NceNetworkPortController.java

package jmri.jmrix.nce;

/*
 * Identifying class representing a NCE communications port
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version $Revision$
 */

public abstract class NceNetworkPortController extends jmri.jmrix.AbstractNetworkPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to NceTrafficController classes, who in turn will deal in messages.
    protected NceSystemConnectionMemo adaptermemo = null;
    
    @Override
    public NceSystemConnectionMemo getSystemConnectionMemo() { 
    	if (adaptermemo == null)
    		adaptermemo = new NceSystemConnectionMemo();
    	return adaptermemo; 
    }
}


/* @(#)NceNetworkPortController.java */

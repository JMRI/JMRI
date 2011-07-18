// EcosPortController.java

package jmri.jmrix.ecos;

/*
 * Identifying class representing a ECOS communications port
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @version $Revision$
 */

public abstract class EcosPortController extends jmri.jmrix.AbstractNetworkPortController {

	// base class. Implementations will provide InputStream and OutputStream
	// objects to EcosTrafficController classes, who in turn will deal in messages.
    protected EcosSystemConnectionMemo adaptermemo = null;
    
    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }
}


/* @(#)EcosPortController.java */

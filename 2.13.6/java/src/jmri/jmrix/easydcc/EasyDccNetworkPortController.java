// EasyDccNetworkPortController.java

package jmri.jmrix.easydcc;

/**
 * Abstract base for classes representing a EasyDcc communications port
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision$
 */
public abstract class EasyDccNetworkPortController extends jmri.jmrix.AbstractNetworkPortController implements jmri.jmrix.NetworkPortAdapter{
	// base class. Implementations will provide InputStream and OutputStream
	// objects to EasyDccTrafficController classes, who in turn will deal in messages.

    protected EasyDccSystemConnectionMemo adaptermemo = null;
    
    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }
}


/* @(#)EasyDccPortController.java */

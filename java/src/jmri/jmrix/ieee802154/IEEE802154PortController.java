// IEEE802154PortController.java

package jmri.jmrix.ieee802154;

import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo;

/**
 * Abstract base for classes representing a communications port
 * @author	Bob Jacobsen    Copyright (C) 2001, 2006, 2007, 2008
 * Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 * Modified for IEEE 802.15.4 connection
 * @author Paul Bender Copyright (C) 2013
 * @version	$Revision$
 */
public abstract class IEEE802154PortController extends jmri.jmrix.AbstractSerialPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to IEEE802154TrafficController classes, who in turn will deal in messages.
    protected IEEE802154SystemConnectionMemo adaptermemo = null;
    
    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }
    
    @Override
    public SystemConnectionMemo getSystemConnectionMemo() { 
    	if (adaptermemo == null)
    		adaptermemo= new IEEE802154SystemConnectionMemo();
    	return adaptermemo; 
    }

    // returns the InputStream from the port
    public abstract java.io.DataInputStream getInputStream();

    // returns the outputStream to the port
    public abstract java.io.DataOutputStream getOutputStream();

    // check that this object is ready to operate
    public abstract boolean status();
}


/* @(#)IEEE802154PortController.java */

// SerialPortController.java

package jmri.jmrix.powerline;

import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.powerline.SerialSystemConnectionMemo;

/**
 * Abstract base for classes representing a communications port
 * @author	Bob Jacobsen    Copyright (C) 2001, 2006, 2007, 2008
 * Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 * @version	$Revision$
 */
public abstract class SerialPortController extends jmri.jmrix.AbstractSerialPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to SerialTrafficController classes, who in turn will deal in messages.
    protected SerialSystemConnectionMemo adaptermemo = null;
    
    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }
    
    @Override
    public SystemConnectionMemo getSystemConnectionMemo() { 
    	if (adaptermemo == null)
    		adaptermemo= new SerialSystemConnectionMemo();
    	return adaptermemo; 
    }
//
//    // returns the InputStream from the port
//    public abstract DataInputStream getInputStream();
//
//    // returns the outputStream to the port
//    public abstract DataOutputStream getOutputStream();
//
//    // check that this object is ready to operate
//    public abstract boolean status();
}


/* @(#)SerialPortController.java */

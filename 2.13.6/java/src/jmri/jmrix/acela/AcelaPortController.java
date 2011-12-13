// AcelaPortController.java

package jmri.jmrix.acela;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a CMRI communications port
 * @author	Bob Jacobsen    Copyright (C) 2001
 * @version	$Revision$
 *
 * @author	Bob Coleman Copyright (C) 2007, 2008
 *              Based on CMRI serial example, modified to establish Acela support. 
 */

public abstract class AcelaPortController extends jmri.jmrix.AbstractSerialPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to AcelaTrafficController classes, who in turn will deal in messages.

    // returns the InputStream from the port
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    public abstract DataOutputStream getOutputStream();

    // check that this object is ready to operate
    public abstract boolean status();
    
    protected AcelaSystemConnectionMemo adaptermemo = null;

    public void setDisabled(boolean disabled) {
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }
}

/* @(#)AcelaPortController.java */
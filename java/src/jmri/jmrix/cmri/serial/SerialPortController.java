// SerialPortController.java

package jmri.jmrix.cmri.serial;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a CMRI communications port
 * @author	Bob Jacobsen    Copyright (C) 2001
 * @version	$Revision: 17977 $
 */
public abstract class SerialPortController extends jmri.jmrix.AbstractSerialPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to SerialTrafficController classes, who in turn will deal in messages.

    // returns the InputStream from the port
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    public abstract DataOutputStream getOutputStream();

    // check that this object is ready to operate
    public abstract boolean status();
}


/* @(#)SerialPortController.java */

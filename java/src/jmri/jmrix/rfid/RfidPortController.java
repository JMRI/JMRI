// RfidPortController.java

package jmri.jmrix.rfid;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a communications port
 * @author	Bob Jacobsen    Copyright (C) 2001, 2006, 2007, 2008
 * @author      Matthew Harris  Copyright (C) 2011
 * @version	$Revision$
 * @since       2.11.4
 */
public abstract class RfidPortController extends jmri.jmrix.AbstractSerialPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to SerialTrafficController classes, who in turn will deal in messages.

    protected RfidSystemConnectionMemo adapterMemo = null;

    // returns the InputStream from the port
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    public abstract DataOutputStream getOutputStream();

    // check that this object is ready to operate
    @Override
    public abstract boolean status();
}


/* @(#)RfidPortController.java */

// PortController.java

package jmri.jmrix.direct;

import jmri.jmrix.SystemConnectionMemo;

/*
 * Identifying class representing a direct-drive communications port
 * @author			Bob Jacobsen    Copyright (C) 2001, 2004
 * @version $Revision$
 */

public abstract class PortController extends jmri.jmrix.AbstractSerialPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to TrafficController classes, who in turn will deal in messages.

        @Override
    public SystemConnectionMemo getSystemConnectionMemo() {
        return null; // Direct connections do not use the SystemConnectionMemo.
    }
}


/* @(#)TPortController.java */

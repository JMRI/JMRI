package jmri.jmrix.xpa;

import jmri.jmrix.SystemConnectionMemo;

/**
 * Abstract base for classes representing an XPA+Modem communications port
 *
 * @author	Paul Bender Copyright (C) 2004
 */
public abstract class XpaPortController extends jmri.jmrix.AbstractSerialPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to XpaTrafficController classes, who in turn will deal in messages.

    protected XpaPortController(SystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }
}

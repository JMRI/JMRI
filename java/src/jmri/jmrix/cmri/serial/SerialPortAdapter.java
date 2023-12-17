package jmri.jmrix.cmri.serial;

import jmri.SystemConnectionMemo;

/**
 * Abstract base for classes representing a CMRI communications port.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public abstract class SerialPortAdapter extends jmri.jmrix.AbstractSerialPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to SerialTrafficController classes, who in turn will deal in messages.

    protected SerialPortAdapter(SystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    // check that this object is ready to operate
    @Override
    public abstract boolean status();

}

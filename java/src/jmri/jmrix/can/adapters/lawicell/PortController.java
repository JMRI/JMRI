package jmri.jmrix.can.adapters.lawicell;

import jmri.SystemConnectionMemo;
import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Abstract base for classes representing a LAWICELL communications port.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Andrew Crosland 2008
 */
public abstract class PortController extends jmri.jmrix.AbstractSerialPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to TrafficController classes, who in turn will deal in messages.

    protected PortController(SystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    // check that this object is ready to operate
    @Override
    public abstract boolean status();

    @Override
    public CanSystemConnectionMemo getSystemConnectionMemo() {
        return (CanSystemConnectionMemo) super.getSystemConnectionMemo();
    }

}


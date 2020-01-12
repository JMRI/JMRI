package jmri.jmrix.mrc;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a MRC communications port
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Kevin Dickerson Copyright (C) 2014
 */
public abstract class MrcPortController extends jmri.jmrix.AbstractSerialPortController {

    // base class. Implementations will provide InputStream and OutputStream
    // objects to MrcTrafficController classes, who in turn will deal in messages.
    protected MrcPortController(MrcSystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    // check that this object is ready to operate
    @Override
    abstract public boolean status();

    // returns the InputStream from the port
    @Override
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    @Override
    public abstract DataOutputStream getOutputStream();

    public boolean okToSend() {
        return true;
    }

    @Override
    public MrcSystemConnectionMemo getSystemConnectionMemo() {
        return (MrcSystemConnectionMemo) super.getSystemConnectionMemo();
    }
}




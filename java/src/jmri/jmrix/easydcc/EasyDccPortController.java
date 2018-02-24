package jmri.jmrix.easydcc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Abstract base for classes representing an EasyDCC communications port.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public abstract class EasyDccPortController extends jmri.jmrix.AbstractSerialPortController {
    // Base class. Implementations will provide InputStream and OutputStream
    // objects to EasyDccTrafficController classes, who in turn will deal in messages.

    protected EasyDccPortController(SystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    // returns the InputStream from the port
    @Override
    abstract public DataInputStream getInputStream();

    // returns the outputStream to the port
    @Override
    abstract public DataOutputStream getOutputStream();

    // check that this object is ready to operate
    @Override
    abstract public boolean status();

    @Override
    public EasyDccSystemConnectionMemo getSystemConnectionMemo() {
        return (EasyDccSystemConnectionMemo) super.getSystemConnectionMemo();
    }

}

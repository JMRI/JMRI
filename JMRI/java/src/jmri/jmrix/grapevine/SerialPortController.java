package jmri.jmrix.grapevine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Abstract base for classes representing a communications port.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006, 2007
 */
public abstract class SerialPortController extends jmri.jmrix.AbstractSerialPortController {

    // base class. Implementations will provide InputStream and OutputStream
    // objects to SerialTrafficController classes, who in turn will deal in messages.
    protected SerialPortController(SystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DataInputStream getInputStream();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DataOutputStream getOutputStream();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean status();

    @Override
    public GrapevineSystemConnectionMemo getSystemConnectionMemo() {
        return (GrapevineSystemConnectionMemo) super.getSystemConnectionMemo();
    }

}

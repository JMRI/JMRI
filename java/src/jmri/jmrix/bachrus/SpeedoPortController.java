package jmri.jmrix.bachrus;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a Bachrus speedo communications port
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Andrew Crosland Copyright (C) 2010
 */
public abstract class SpeedoPortController extends jmri.jmrix.AbstractSerialPortController {

    // base class. Implementations will provide InputStream and OutputStream
    // objects to SprogTrafficController classes, who in turn will deal in messages.
    protected SpeedoPortController(SpeedoSystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    // returns the InputStream from the port
    @Override
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    @Override
    public abstract DataOutputStream getOutputStream();

    // check that this object is ready to operate
    @Override
    public abstract boolean status();

    @Override
    public SpeedoSystemConnectionMemo getSystemConnectionMemo() {
        return (SpeedoSystemConnectionMemo) super.getSystemConnectionMemo();
    }

}

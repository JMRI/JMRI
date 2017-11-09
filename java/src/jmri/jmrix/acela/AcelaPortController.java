package jmri.jmrix.acela;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing an Acela communications port
 *
 * @author Bob Jacobsen Copyright (C) 2001
  *
 * @author Bob Coleman Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public abstract class AcelaPortController extends jmri.jmrix.AbstractSerialPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to AcelaTrafficController classes, who in turn will deal in messages.

    protected AcelaPortController(AcelaSystemConnectionMemo memo) {
        super(memo);
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
    public AcelaSystemConnectionMemo getSystemConnectionMemo() {
        return (AcelaSystemConnectionMemo) super.getSystemConnectionMemo();
    }

}

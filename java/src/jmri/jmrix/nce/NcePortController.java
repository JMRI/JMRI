package jmri.jmrix.nce;

/*
 * Identifying class representing a NCE communications port.
 *
 * @author   Bob Jacobsen    Copyright (C) 2001
 */
public abstract class NcePortController extends jmri.jmrix.AbstractSerialPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to NceTrafficController classes, who in turn will deal in messages.

    protected NcePortController(NceSystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    @Override
    public NceSystemConnectionMemo getSystemConnectionMemo() {
        return (NceSystemConnectionMemo) super.getSystemConnectionMemo();
    }

}

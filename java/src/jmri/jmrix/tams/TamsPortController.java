package jmri.jmrix.tams;

/**
 * Identifying class representing a Tams communications port Based on work by
 * Bob Jacobsen
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 */
public abstract class TamsPortController extends jmri.jmrix.AbstractSerialPortController {

    // base class. Implementations will provide InputStream and OutputStream
    // objects to TamsTrafficController classes, who in turn will deal in messages.
    protected TamsPortController(TamsSystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    @Override
    public TamsSystemConnectionMemo getSystemConnectionMemo() {
        return (TamsSystemConnectionMemo) super.getSystemConnectionMemo();
    }
}




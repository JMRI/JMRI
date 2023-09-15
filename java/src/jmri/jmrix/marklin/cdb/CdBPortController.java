package jmri.jmrix.marklin.cdb;

/**
 * Identifying class representing a Marklin CDB communications port Based on work by
 * Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public abstract class CdBPortController extends jmri.jmrix.AbstractSerialPortController {

    // base class. Implementations will provide InputStream and OutputStream
    // objects to CdBTrafficController classes, who in turn will deal in messages.
    protected CdBPortController(CdBSystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    @Override
    public CdBSystemConnectionMemo getSystemConnectionMemo() {
        return (CdBSystemConnectionMemo) super.getSystemConnectionMemo();
    }
}




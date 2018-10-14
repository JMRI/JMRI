package jmri.jmrix.dcc4pc;

/*
 * Identifying class representing a DCC4PC communications port
 * @author          Kevin Dickerson Copyright (C) 2012
 * @author   Bob Jacobsen    Copyright (C) 2001, 2008
 * 
 */
public abstract class Dcc4PcPortController extends jmri.jmrix.AbstractSerialPortController {

    // base class. Implementations will provide InputStream and OutputStream
    // objects to Dcc4PcTrafficController classes, who in turn will deal in messages.
    protected Dcc4PcPortController(Dcc4PcSystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    @Override
    public Dcc4PcSystemConnectionMemo getSystemConnectionMemo() {
        return (Dcc4PcSystemConnectionMemo) super.getSystemConnectionMemo();
    }
}

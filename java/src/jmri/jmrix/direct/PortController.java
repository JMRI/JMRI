package jmri.jmrix.direct;

/*
 * Identifying class representing a direct-drive communications port
 * @author   Bob Jacobsen    Copyright (C) 2001, 2004
 */
public abstract class PortController extends jmri.jmrix.AbstractSerialPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to TrafficController classes, who in turn will deal in messages.

    protected PortController() {
        super(new DirectSystemConnectionMemo());
        this.manufacturerName = jmri.jmrix.OtherConnectionTypeList.OTHER;
    }

}


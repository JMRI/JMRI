// SerialPortController.java
package jmri.jmrix.powerline;

/**
 * Abstract base for classes representing a communications port
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2006, 2007, 2008 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 * @version	$Revision$
 */
public abstract class SerialPortController extends jmri.jmrix.AbstractSerialPortController {

    // base class. Implementations will provide InputStream and OutputStream
    // objects to SerialTrafficController classes, who in turn will deal in messages.
    protected SerialPortController(SerialSystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    @Override
    public SerialSystemConnectionMemo getSystemConnectionMemo() {
        return (SerialSystemConnectionMemo) super.getSystemConnectionMemo();
    }

}


/* @(#)SerialPortController.java */

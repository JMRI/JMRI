package jmri.jmrix.qsi;

/**
 * Abstract base for classes representing a QSI communications port.
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 */
public abstract class QsiPortController extends jmri.jmrix.AbstractSerialPortController {

    // base class. Implementations will provide InputStream and OutputStream
    // objects to QsiTrafficController classes, who in turn will deal in messages.
    protected QsiPortController(QsiSystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    @Override
    public QsiSystemConnectionMemo getSystemConnectionMemo() {
        return (QsiSystemConnectionMemo) super.getSystemConnectionMemo();
    }

}

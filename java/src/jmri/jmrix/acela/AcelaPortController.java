package jmri.jmrix.acela;

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

    // check that this object is ready to operate
    @Override
    public abstract boolean status();

    @Override
    public AcelaSystemConnectionMemo getSystemConnectionMemo() {
        return (AcelaSystemConnectionMemo) super.getSystemConnectionMemo();
    }

}

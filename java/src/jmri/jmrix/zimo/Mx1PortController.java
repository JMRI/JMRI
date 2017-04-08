package jmri.jmrix.zimo;

/**
 * Abstract base for classes representing a MX-1 communications port. Adapted by
 * Sip Bosch for use with zimo Mx-1.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public abstract class Mx1PortController extends jmri.jmrix.AbstractSerialPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to Mx1TrafficController classes, who in turn will deal in messages.

    protected Mx1PortController(Mx1SystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    /**
     * Can the port accept additional characters? This might go false for short
     * intervals, but it might also stick off if something goes wrong.
     *
     * @return true if more data can be sent; false otherwise
     */
    public abstract boolean okToSend();

    @Override
    public Mx1SystemConnectionMemo getSystemConnectionMemo() {
        return (Mx1SystemConnectionMemo) super.getSystemConnectionMemo();
    }
}

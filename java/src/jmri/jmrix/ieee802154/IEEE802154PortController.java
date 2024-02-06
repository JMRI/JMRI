package jmri.jmrix.ieee802154;

/**
 * Abstract base for classes representing a communications port
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006, 2007, 2008 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011 Modified for IEEE 802.15.4 connection
 * @author Paul Bender Copyright (C) 2013,2023
 */
public abstract class IEEE802154PortController extends jmri.jmrix.AbstractSerialPortController {

    // base class. Implementations will provide InputStream and OutputStream
    // objects to IEEE802154TrafficController classes, who in turn will deal in messages.
    protected IEEE802154PortController(IEEE802154SystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    @Override
    public IEEE802154SystemConnectionMemo getSystemConnectionMemo() {
        return (IEEE802154SystemConnectionMemo) super.getSystemConnectionMemo();
    }

    // check that this object is ready to operate
    @Override
    public abstract boolean status();

}

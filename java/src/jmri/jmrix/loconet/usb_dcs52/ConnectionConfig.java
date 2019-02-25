package jmri.jmrix.loconet.usb_dcs52;

import jmri.util.SystemType;

/**
 * Definition of objects to handle configuring a DCS52 USB layout connection via a
 * PR2Adapter object.
 * <p>
 * Copied from loconet.pr3.ConnectionConfig.java
 * <p>
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2008, 2010
 * @author B. Milhaupt Copyright (C) 2019
 */

public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no preexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "DCS52 USB Interface"; // NOI18N
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new UsbDcs52Adapter();
        }
    }
}

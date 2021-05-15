package jmri.jmrix.loconet.uhlenbrock.usb_63120;

import jmri.util.SystemType;

/**
 * Definition of objects to handle configuring a Uhlenbrock USB-adapter 63120 layout
 * connection via a UsbUhlenbrock63120Adapter object. Confirmed to work 02/2021.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Egbert Broerse Copyright (C) 2020
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     * @param p the SerialPortAdapter to associate with this connection
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return Bundle.getMessage("USB_63120Title");
    }

    public boolean isOptList2Advanced() {
        return false;
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new UsbUhlenbrock63120Adapter();
        }
    }

    @Override
    protected String[] getPortFriendlyNames() {
        if (SystemType.isWindows()) {
            return new String[]{"Interface-63120", "Interface"}; // NOI18N
        }
        return new String[]{};
    }

}

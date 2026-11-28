package jmri.jmrix.loconet.uhlenbrock.usb_63860;

import jmri.util.SystemType;

/**
 * Definition of objects to handle configuring a connection via the USB port
 * of an Uhlenbrock LocoNet-USB and WLAN Interface 63860, using a
 * {@link Usb63860Adapter} object.
 *
 * @author detami01 Copyright (C) 2026
 * @see jmri.jmrix.loconet.uhlenbrock.usb_63120.ConnectionConfig
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
        return Bundle.getMessage("USB_63860Title");
    }

    public boolean isOptList2Advanced() {
        return false;
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new Usb63860Adapter();
        }
    }

    /**
     * The 63860 uses a Silicon Labs CP210x USB to UART bridge.
     * {@inheritDoc}
     */
    @Override
    protected String[] getPortFriendlyNames() {
        if (SystemType.isWindows()) {
            return new String[]{"CP210x", "Silicon Labs"}; // NOI18N
        }
        return new String[]{};
    }

}

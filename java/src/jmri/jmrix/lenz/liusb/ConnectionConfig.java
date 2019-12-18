package jmri.jmrix.lenz.liusb;

import jmri.util.SystemType;

/**
 * Handle configuring an XpressNet layout connection via a Lenz LIUSBadapter.
 * <p>
 * This uses the {@link LIUSBAdapter} class to do the actual connection.
 *
 * @author Paul Bender Copyright (C) 2005
 *
 * @see LIUSBAdapter
 */
public class ConnectionConfig extends jmri.jmrix.lenz.AbstractXNetSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process.
     * Swing init is deferred.
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
        return "Lenz LIUSB"; // NOI18N
    }

    @Override
    protected String[] getPortFriendlyNames() {
        if (SystemType.isWindows()) {
            return new String[]{Bundle.getMessage("LIUSBSerialPortOption"), "LI-USB"};
        }
        return new String[]{};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new LIUSBAdapter();
        }
    }

}

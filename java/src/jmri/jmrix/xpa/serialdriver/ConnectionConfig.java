package jmri.jmrix.xpa.serialdriver;

/**
 * Definition of objects to handle configuring a layout connection via an
 * XPA+Modem SerialDriverAdapter object.
 *
 * @author Paul Bender Copyright (C) 2004
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Create a connection with an existing adapter.
     *
     * @param p the serial port adapter
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
        return "XPA-MODEM"; // NOI18N
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new SerialDriverAdapter();
        }
    }

}

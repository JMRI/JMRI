package jmri.jmrix.wangrow.serialdriver;

import jmri.jmrix.nce.serialdriver.SerialDriverAdapter;

/**
 * Definition of objects to handle configuring a layout connection via an
 * Wangrow SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    public final static String NAME = Bundle.getMessage("TypeSerial");

    /**
     * Create a connection configuration with a preexisting adapter.
     *
     * @param p the adapter associated with the connection
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
        return NAME;
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

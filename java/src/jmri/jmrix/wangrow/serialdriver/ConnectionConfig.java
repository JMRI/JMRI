package jmri.jmrix.wangrow.serialdriver;

import jmri.jmrix.nce.serialdriver.SerialDriverAdapter;

/**
 * Definition of objects to handle configuring a layout connection via an
 * Wangrow SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    public final static String NAME = "Serial";

    /**
     * Create a connection configuration with a preexisting adapter.
     *
     * @param p the adapter associated with the connection
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Create a connection configuration with no preexisting adapter.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new SerialDriverAdapter();
        }
    }
}

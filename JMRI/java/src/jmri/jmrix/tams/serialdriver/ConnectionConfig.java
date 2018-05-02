package jmri.jmrix.tams.serialdriver;

/**
 * Definition of objects to handle configuring a layout connection via an TAMS
 * SerialDriverAdapter object.
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    public final static String NAME = "MasterControl";

    /**
     * Create a connection configuration with a preexisting adapter. This is
     * used principally when loading a configuration that defines this
     * connection.
     *
     * @param p the adapter to create a connection configuration for
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Create a connection configuration without a preexisting adapter.
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

package jmri.jmrix.dcc4pc.serialdriver;

/**
 * Definition of objects to handle configuring a layout connection via an DCC4PC
 * SerialDriverAdapter object.
 *
 * @author Kevin Dickerson Copyright (C) 2001, 2003
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
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "DCC4PC";
    }

    public boolean isOptList1Advanced() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("deprecation") // until DCC4PC is migrated to multiple systems
    protected void setInstance() {
        adapter = SerialDriverAdapter.instance();
    }

}

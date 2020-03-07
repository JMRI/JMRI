package jmri.jmrix.loconet.ms100;

/**
 * Definition of objects to handle configuring a LocoBuffer layout connection
 * via a LocoNet MS100Adapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     *
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

    /**
     * Provide this adapter name, if it's available on this system.
     *
     * @return null if this is a macOS system that can't run MS100
     */
    @Override
    public String name() {
        return "LocoNet MS100";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new MS100Adapter();
        }
    }

}

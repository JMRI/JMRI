package jmri.jmrix.tmcc.serialdriver;

/**
 * Definition of objects to handle configuring a TMCC layout connection
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Create a connection configuration with an existing adapter.
     *
     * @param p the adapter
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
        return Bundle.getMessage("AdapterSerialName");
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new SerialDriverAdapter();
        }
    }

}

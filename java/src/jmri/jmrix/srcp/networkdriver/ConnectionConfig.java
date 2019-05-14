package jmri.jmrix.srcp.networkdriver;

/**
 * Definition of objects to handle configuring an EasyDCC layout connection via
 * a NetworkDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 */
public class ConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    /**
     * Create a connection configuration with a preexisting adapter. This is
     * used principally when loading a configuration that defines this
     * connection.
     *
     * @param p the adapter to create a connection configuration for
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
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
        return "SRCP Network Connection";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new NetworkDriverAdapter();
            adapter.setPort(4303); // 4303 is assigned to SRCP by IANA
        }
    }

    @Override
    public boolean isPortAdvanced() {
        return false;
    }

}

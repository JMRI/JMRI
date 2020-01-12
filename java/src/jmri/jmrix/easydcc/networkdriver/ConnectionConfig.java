package jmri.jmrix.easydcc.networkdriver;

import jmri.jmrix.JmrixConfigPane;

/**
 * Definition of objects to handle configuring an EasyDCC layout connection via
 * a NetworkDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 */
public class ConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
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
        return Bundle.getMessage("AdapterNetworkName");
    }

    /**
     * Reimplement this method to show the connected host, rather than the usual
     * port name.
     *
     * @return human-readable connection information
     */
    @Override
    public String getInfo() {
        String t = adapter.getHostName();
        if (t != null && !t.equals("")) {
            return t;
        } else {
            return JmrixConfigPane.NONE;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new NetworkDriverAdapter();
        }
    }

    @Override
    public boolean isPortAdvanced() {
        return false;
    }

}

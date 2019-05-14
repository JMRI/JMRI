package jmri.jmrix.loconet.loconetovertcp;


/**
 * Definition of objects to handle configuring a LocoNetOverTcp layout
 * connection via a LnTcpDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Stephen Williams Copyright (C) 2008
 *
 */
public class ConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     * @param p the NetworkPortAdapter to associate with this connection
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
        return Bundle.getMessage("ConnectionTitle");
    }

    public boolean isOptList1Advanced() {
        return false;
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new LnTcpDriverAdapter();
            adapter.setPort(1234);
        }
    }

}

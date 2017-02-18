package jmri.jmrix.srcp.networkdriver;

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
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "SRCP Network Connection";
    }

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

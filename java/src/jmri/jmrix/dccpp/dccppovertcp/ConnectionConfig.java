package jmri.jmrix.dccpp.dccppovertcp;

import jmri.jmrix.dccpp.DCCppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Definition of objects to handle configuring a DCC++OverTcp layout
 * connection via a DCCppTcpDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Stephen Williams Copyright (C) 2008
 * @author Mark Underwood Copyright (C) 2015
 *
 * Based on LocoNetOverTCP 
 */
public class ConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig() {
        super();
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);
        log.info("NetworkPortAdapter opening. Is DCC++ Over TCP Server running on host?");
    }

    @Override
    public String name() {
        return "DCC++ Server";
    }

    public boolean isOptList1Advanced() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new DCCppTcpDriverAdapter();
            adapter.setPort(DCCppConstants.DCCPP_OVER_TCP_PORT); // TODO: Choose another port?
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ConnectionConfig.class);

}

// ConnectionConfig.java
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
 * @version $Revision$
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
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);
	log.debug("NetworkPortAdapter ConnectionConfig Ctor called. ");
    }

    public String name() {
        return "DCC++ Server";
    }

    public boolean isOptList1Advanced() {
        return false;
    }

    protected void setInstance() {
        if (adapter == null) {
            adapter = new DCCppTcpDriverAdapter();
            adapter.setPort(DCCppConstants.DCCPP_OVER_TCP_PORT); // TODO: Choose another port?
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ConnectionConfig.class.getName());
}

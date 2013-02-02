// ConnectionConfig.java

package jmri.jmrix.loconet.loconetovertcp;

import org.apache.log4j.Logger;

/**
 * Definition of objects to handle configuring a LocoNetOverTcp layout connection
 * via a LnTcpDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @author      Stephen Williams Copyright (C) 2008
 *
 * @version     $Revision$
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractNetworkConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p){
        super(p);
    }
    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }
    
    public String name() { return "LocoNetOverTcp LbServer"; }
    
    public boolean isOptList1Advanced() { return false; }
    
    protected void setInstance() {
        if (adapter==null){
            adapter = new LnTcpDriverAdapter();
            adapter.setPort(1234);
        }
    }

    static Logger log = Logger.getLogger(ConnectionConfig.class.getName());
}


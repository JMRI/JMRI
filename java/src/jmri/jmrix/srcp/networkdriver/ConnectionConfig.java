// ConnectionConfig.java

package jmri.jmrix.srcp.networkdriver;

import jmri.jmrix.JmrixConfigPane;

/**
 * Definition of objects to handle configuring an EasyDCC layout connection
 * via a NetworkDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.7 $
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

    public String name() { return "Network Connection"; }

    /**
     * Reimplement this method to show the connected host,
     * rather than the usual port name.
     * @return human-readable connection information
     */
    public String getInfo() {
        String t = adapter.getHostName();
        if (t != null && !t.equals("")) return t;
        else return JmrixConfigPane.NONE;
    }
    protected void setInstance() {
        adapter = NetworkDriverAdapter.instance();
    }

    public boolean isPortAdvanced() {return false;}
    
}


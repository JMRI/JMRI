// ConnectionConfig.java

package jmri.jmrix.jmriclient.networkdriver;

/**
 * Definition of objects to handle configuring a connection to a remote
 * JMRI instance via the JMRI Network Protocol.
 *
 * @author      Paul Bender   Copyright (C) 2010
 * @version	$Revision$
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractNetworkConnectionConfig {

    /**
     * Constructor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p){
        super(p);
    }
    /**
     * Constructor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    public String name() { return "JMRI Network Connection"; }

    protected void setInstance() {
      if (adapter==null){
        adapter = new NetworkDriverAdapter();
      }
    }

    public boolean isPortAdvanced() {return true;}
    
}


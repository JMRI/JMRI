// ConnectionConfig.java

package jmri.jmrix.tmcc.serialdriver;

/**
 * Definition of objects to handle configuring a TMCC layout connection
 *
 * @author      Bob Jacobsen   Copyright (C) 2003, 2006
 * @version	$Revision: 1.2 $
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        super(p);
    }
    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }
	
    public String name() { return "TMCC (Lionel)"; }

    protected void setInstance() { adapter = SerialDriverAdapter.instance(); }
}


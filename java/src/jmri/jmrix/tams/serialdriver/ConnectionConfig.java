// ConnectionConfig.java

package jmri.jmrix.tams.serialdriver;

/**
 * Definition of objects to handle configuring a layout connection
 * via an TAMS SerialDriverAdapter object.
 *
 * @author      Kevin Dickerson   Copyright (C) 2012
 * @version	$Revision: 17977 $
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractSerialConnectionConfig {

	public final static String NAME = "MasterControl";
	
    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        super(p);
    }
    /**
     * Ctor for a functional Swing object with no existing adapter
     */
    public ConnectionConfig() {
        super();
    }

    public String name() { return NAME; }
    
    protected void setInstance() {
        if (adapter == null)
            adapter = new SerialDriverAdapter();
    }

}


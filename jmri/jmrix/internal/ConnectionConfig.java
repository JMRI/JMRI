// ConnectionConfig.java

package jmri.jmrix.internal;

/**
 * Definition of objects to handle configuring a virtual layout connection
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003, 2010
 * @version	$Revision: 1.4 $
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractSimulatorConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no existing adapter
     */
    public ConnectionConfig() {
        super();
    }
    
    public String name() { return "No Connection"; }
    
    protected void setInstance() {
        if (adapter == null)
            adapter = new InternalAdapter();
    }
    
    String manufacturerName = jmri.jmrix.DCCManufacturerList.NONE;
    
    public void dispose() {
        if (adapter != null)
            adapter.dispose();
        super.dispose();
    }
    
}


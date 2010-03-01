// ConnectionConfig.java

package jmri.jmrix.internal;

import javax.swing.*;

/**
 * Definition of objects to handle configuring a virtual layout connection
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003, 2010
 * @version	$Revision: 1.3 $
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     */
    public ConnectionConfig() {
        super();
    }

    public String name() { return "No Connection"; }

	/**
	 * Don't need parent implementation of this
	 */
	public void loadDetails(final JPanel details) {
    }
    
    protected void setInstance() {
        log.error("Unexpected call to setInstance");
        new Exception().printStackTrace();
    }
    
    String manufacturerName = jmri.jmrix.DCCManufacturerList.NONE;
    
    public String getManufacturer() { return manufacturerName; }
    public void setManufacturer(String manu) { manufacturerName=manu; }
}


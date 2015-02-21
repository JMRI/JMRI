// ConnectionConfig.java
package jmri.jmrix.nce.networkdriver;

/**
 * Definition of objects to handle configuring an NCE layout connection via a
 * NetworkDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @version	$Revision$
 */
public class ConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    public final static String NAME = "Network Interface";

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no existing adapter
     */
    public ConnectionConfig() {
        super();
    }

    public String name() {
        return NAME;
    }
   // String manufacturerName = jmri.jmrix.DCCManufacturerList.NCE;

    // public String getManufacturer() { return manufacturerName; }
    // public void setManufacturer(String manu) { manufacturerName=manu; }
    protected void setInstance() {
        if (adapter == null) {
            adapter = new NetworkDriverAdapter();
        }
    }
}

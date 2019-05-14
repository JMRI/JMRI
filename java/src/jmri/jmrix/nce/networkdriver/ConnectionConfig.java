package jmri.jmrix.nce.networkdriver;

/**
 * Definition of objects to handle configuring an NCE layout connection via a
 * NetworkDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 */
public class ConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    public final static String NAME = "Network Interface";

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     * @param p SerialPortAdapter for existing adapter
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

    @Override
    public String name() {
        return NAME;
    }
   // String manufacturerName = jmri.jmrix.DCCManufacturerList.NCE;

    // public String getManufacturer() { return manufacturerName; }
    // public void setManufacturer(String manu) { manufacturerName=manu; }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new NetworkDriverAdapter();
        }
    }

}

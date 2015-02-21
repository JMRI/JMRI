// ConnectionConfig.java
package jmri.jmrix.lenz.xnetsimulator;

/**
 * Handle configuring an XPressNet layout connection via a XNetSimulator
 * adapter.
 * <P>
 * This uses the {@link XNetSimulatorAdapter} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Paul Bender Copyright (C) 2009
 * @version	$Revision$
 *
 * @see XNetSimulatorAdapter
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSimulatorConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    public String name() {
        return "XPressNet Simulator";
    }

    String manufacturerName = "Lenz";

    public String getManufacturer() {
        return manufacturerName;
    }

    public void setManufacturer(String manu) {
        manufacturerName = manu;
    }

    protected void setInstance() {
        if (adapter == null) {
            adapter = new XNetSimulatorAdapter();
        }
    }
}

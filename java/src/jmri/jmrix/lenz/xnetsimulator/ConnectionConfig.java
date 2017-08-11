package jmri.jmrix.lenz.xnetsimulator;

/**
 * Handle configuring an XpressNet layout connection via a XNetSimulator
 * adapter.
 * <P>
 * This uses the {@link XNetSimulatorAdapter} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Paul Bender Copyright (C) 2009
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
     * Ctor for a functional Swing object with no preexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return Bundle.getMessage("XNetSimulatorName");
    }

    String manufacturerName = "Lenz"; // NOI18N

    @Override
    public String getManufacturer() {
        return manufacturerName;
    }

    @Override
    public void setManufacturer(String manu) {
        manufacturerName = manu;
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new XNetSimulatorAdapter();
        }
    }

}

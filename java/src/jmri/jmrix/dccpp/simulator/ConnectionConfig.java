package jmri.jmrix.dccpp.simulator;

/**
 * Handle configuring a DCC++ layout connection via a DCCppSimulator
 * adapter.
 * <p>
 * This uses the {@link DCCppSimulatorAdapter} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Paul Bender Copyright (C) 2009
 * @author Mark Underwood Copyright (C) 2015
  *
 * @see DCCppSimulatorAdapter
 *
 * Based on jmri.jmrix.lenz.xnetsimulator.ConnectionConfig
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
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "DCC++ Simulator";
    }

    String manufacturerName = "DCC++";

    @Override
    public String getManufacturer() {
        return manufacturerName;
    }

    @Override
    public void setManufacturer(String manu) {
        manufacturerName = manu;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new DCCppSimulatorAdapter();
        }
    }

}

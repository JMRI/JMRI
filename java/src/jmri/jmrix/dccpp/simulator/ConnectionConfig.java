package jmri.jmrix.dccpp.simulator;

import jmri.jmrix.AbstractSimulatorConnectionConfig;
import jmri.jmrix.SerialPortAdapter;

/**
 * Handle configuring a DCC++ layout connection via a DCCppSimulator
 * adapter.
 * <P>
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
public class ConnectionConfig extends AbstractSimulatorConnectionConfig<SerialPortAdapter> {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
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

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new DCCppSimulatorAdapter();
        }
    }
}

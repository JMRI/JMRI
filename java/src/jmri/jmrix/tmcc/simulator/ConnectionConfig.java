package jmri.jmrix.tmcc.simulator;

/**
 * Handle configuring an TMCC layout connection via a SimulatorAdapter
 * adapter.
 * <p>
 * This uses the {@link SimulatorAdapter} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Paul Bender Copyright (C) 2009
 * @author Mark Underwood Copyright (C) 2015
  *
 * @see SimulatorAdapter
 *
 * Based on jmri.jmrix.lenz.xnetsimulator.ConnectionConfig, copied from DCCpp, EasyDCC
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

    @Override
    public String name() {
        return "TMCC Simulator";
    }

    String manufacturerName = jmri.jmrix.tmcc.SerialConnectionTypeList.LIONEL;

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
            adapter = new SimulatorAdapter();
        }
    }

}

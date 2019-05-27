package jmri.jmrix.direct.simulator;

import javax.swing.JButton;
import javax.swing.JPanel;
import jmri.jmrix.direct.DirectSystemConnectionMemo;

/**
 * Handle configuring an Direct layout connection via a Simulator
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
 * Based on jmri.jmrix.lenz.xnetsimulator.ConnectionConfig, copied from EasyDCC
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
        return Bundle.getMessage("DirectSimulatorName");
    }

    String manufacturerName = jmri.jmrix.direct.DirectConnectionTypeList.DIRECT;

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
            adapter = new SimulatorAdapter();
        }
    }

}

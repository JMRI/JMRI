package jmri.jmrix.tams.simulator;

import javax.swing.JPanel;

/**
 * Definition of objects to handle configuring a layout connection via a Tams
 * SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003 Copies from NCE
 * @author kcameron Copyright (C) 2014
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSimulatorConnectionConfig {

    public final static String NAME = "Simulator";

    /**
     * Create a connection configuration with a preexisting adapter. This is
     * used principally when loading a configuration that defines this
     * connection.
     *
     * @param p the adapter to create a connection configuration for
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Create a connection configuration without a preexisting adapter.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void loadDetails(JPanel details) {
        super.loadDetails(details);
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new SimulatorAdapter();
        }
    }
}

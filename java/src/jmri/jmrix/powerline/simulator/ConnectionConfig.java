package jmri.jmrix.powerline.simulator;

import javax.swing.JPanel;

/**
 * Definition of objects to handle configuring a layout connection via a
 * Powerline Simulator object.
 *
 * @author Ken Cameron Copyright (C) 2011 based on NceSimulator by Bob Jacobson
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSimulatorConnectionConfig {

    public final static String NAME = Bundle.getMessage("PlSimulatorName");

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     * @param p port adapter for simulator
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no existing adapter.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(JPanel details) {
        super.loadDetails(details);
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

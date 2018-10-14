package jmri.jmrix.can.adapters.loopback;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Definition of objects to handle configuring a layout connection via a LocoNet
 * hexfile emulator
 *
 * @author Bob Jacobsen Copyright (C) 2008
  */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "name assigned historically")
public class ConnectionConfig extends jmri.jmrix.can.adapters.ConnectionConfig {

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

    // Needed for instantiation by reflection, do not remove.
    /**
     * Create a connection configuration without a preexisting adapter.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "CAN Simulation";
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new Port();
        }
    }
}

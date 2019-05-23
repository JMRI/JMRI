package jmri.jmrix.can.adapters.loopback;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.swing.JPanel;

/**
 * Definition of objects to handle configuring a layout connection via a CAN
 * hexfile emulator.
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
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public ConnectionConfig() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(JPanel details) {
        super.loadDetails(details);
        portBoxLabel.setVisible(false); // hide portBoxLabel during config, as when extending jmri.jmrix.AbstractSimulatorConnectionConfig
        portBox.setVisible(false);      // hide portBox combo idem
        baudBoxLabel.setVisible(false); // hide baudBoxLabel idem
        baudBox.setVisible(false);      // hide baudBox combo idem
    }

    @Override
    public String name() {
        return Bundle.getMessage("CanSimulationName");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new Port();
        }
    }

}

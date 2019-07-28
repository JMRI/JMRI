package jmri.jmrix.roco.z21.simulator;

import javax.swing.JPanel;


/**
 * Handle configuring an z21 layout connection via a z21Simulator
 * adapter.
 * <p>
 * This uses the {@link Z21SimulatorAdapter} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Paul Bender Copyright (C) 2009
 *
 * @see Z21SimulatorAdapter
 */
public class Z21SimulatorConnectionConfig extends jmri.jmrix.roco.z21.ConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public Z21SimulatorConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public Z21SimulatorConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "Z21 Simulator";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(JPanel details) {
        super.loadDetails(details);
        hostNameField.setText("localhost");
        hostNameField.setEnabled(false); // always localhost.
        portField.setEnabled(false); // we don't change this on the simulator.
        portFieldLabel.setText(Bundle.getMessage("CommunicationPortLabel"));
        portField.setText(String.valueOf(adapter.getPort()));
        portField.setEnabled(false); // we can't change this now.
    }

    @Override
    public boolean isHostNameAdvanced() {
        return true;  // hostname is always localhost.
    }

    @Override
    public boolean isAutoConfigPossible() {
        return false;  // always fixed, no reason to search.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new Z21SimulatorAdapter();
        }
    }

}

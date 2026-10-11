package jmri.jmrix.marklin.simulation;

import javax.swing.JPanel;

/**
 * Handle configuring a simulated Marklin CS2 layout connection
 * via a NetworkDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Steve Young Copyright (C) 2024
 */
public class MarklinSimConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     * @param p network port adapter.
     */
    public MarklinSimConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public MarklinSimConnectionConfig() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(final JPanel details) {
        super.loadDetails(details);
        this.updateAdapter();
        portField.setEnabled(false);
        this.hostNameField.setEditable(false);
    }

    @Override
    public String name() {
        return "Marklin Simulation"; // NOI18N
    }

    @Override
    public String getConnectionName() {
        if ( adapter!= null && adapter.getSystemConnectionMemo() != null) {
            return adapter.getSystemConnectionMemo().getUserName();
        } else {
            return null;
        }
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new MarklinSimDriverAdapter();
        }
    }

}

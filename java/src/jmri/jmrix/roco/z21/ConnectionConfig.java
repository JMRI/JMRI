package jmri.jmrix.roco.z21;

import javax.swing.JPanel;

/**
 * Handle configuring an layout connection via a Roco z21 or Z21.
 * <p>
 * This uses the {@link Z21Adapter} class to do the actual connection.
 *
 * @author	Paul Bender Copyright (C) 2011
 *
 * @see Z21Adapter
 */
public class ConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
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
        return "Roco Z21"; // NOI18N
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new Z21Adapter();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(JPanel details) {
        super.loadDetails(details);
        hostNameField.setText(adapter.getHostName());
        portFieldLabel.setText(Bundle.getMessage("CommunicationPortLabel"));
        portField.setText(String.valueOf(adapter.getPort()));
        portField.setEnabled(false); // we can't change this now.
    }

    @Override
    public boolean isHostNameAdvanced() {
        return showAutoConfig.isSelected();
    }

    @Override
    public boolean isAutoConfigPossible() {
        return true;
    }

}

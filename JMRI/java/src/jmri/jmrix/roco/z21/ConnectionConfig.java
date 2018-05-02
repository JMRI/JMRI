package jmri.jmrix.roco.z21;

import javax.swing.JPanel;

/**
 * Handle configuring an layout connection via a Roco z21 or Z21.
 * <P>
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
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "Roco Z21";
    }

    /**
     * Load the adapter with an appropriate object
     * <i>unless</i> it has already been set.
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new Z21Adapter();
        }
    }

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

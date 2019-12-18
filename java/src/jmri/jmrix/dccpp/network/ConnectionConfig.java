package jmri.jmrix.dccpp.network;

import javax.swing.JPanel;

/**
 * Handle configuring a DCC++ layout connection via Ethernet.Port
 * <p>
 * This uses the {@link DCCppEthernetAdapter} class to do the actual connection.
 *
 * @author Paul Bender Copyright (C) 2011
 * @author Mark Underwood Copyright (C) 2015
  *
 * Adapted from LIUSBEthernetAdapter
 * 
 * @see jmri.jmrix.lenz.liusbethernet.LIUSBEthernetAdapter
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
     * Ctor for a functional Swing object with no pre-existing adapter
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "DCC++ Ethernet";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new DCCppEthernetAdapter();
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
        //opt1Box.setEnabled(false); // we can't change this now.
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

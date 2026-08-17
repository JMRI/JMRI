package jmri.jmrix.dccpp.network;

import java.awt.Component;
import java.awt.GridBagConstraints;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * Handle configuring a DCC-EX layout connection via Ethernet.Port
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

    private final JCheckBox autoReconnectCheckBox =
            new JCheckBox(Bundle.getMessage("AutoReconnectLabel"));

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     * @param p network port adapter.
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
        return "DCC-EX Ethernet";
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
        portField.setEnabled(true);

        autoReconnectCheckBox.setSelected(adapter.getAllowConnectionRecovery());
        if (autoReconnectCheckBox.getItemListeners().length == 0) {
            autoReconnectCheckBox.addItemListener(e -> {
                boolean enabled = autoReconnectCheckBox.isSelected();
                adapter.setAllowConnectionRecovery(enabled);
                if (enabled) {
                    adapter.setReconnectMaxAttempts(-1);
                }
            });
        }
    }

    @Override
    protected void showAdvancedItems() {
        super.showAdvancedItems();
        if (showAdvanced.isSelected()) {
            int maxRow = -1;
            for (Component c : _details.getComponents()) {
                GridBagConstraints gbc = gbLayout.getConstraints(c);
                if (gbc.gridy > maxRow) {
                    maxRow = gbc.gridy;
                }
            }
            cL.gridy = maxRow + 1;
            cL.gridwidth = 2;
            // Field initializer created before L&F is ready on some platforms; sync font/paint now.
            autoReconnectCheckBox.updateUI();
            gbLayout.setConstraints(autoReconnectCheckBox, cL);
            _details.add(autoReconnectCheckBox);
            cL.gridwidth = 1;
            _details.revalidate();
        }
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

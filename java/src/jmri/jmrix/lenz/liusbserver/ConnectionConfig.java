package jmri.jmrix.lenz.liusbserver;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;

/**
 * Handle configuring an XpressNet layout connection via a LIUSB Server.
 * <P>
 * This uses the {@link LIUSBServerAdapter} class to do the actual connection.
 *
 * @author Paul Bender Copyright (C) 2009
  *
 * @see LIUSBServerAdapter
 */
public class ConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    /**
     * Ctor for an object being created during load process.
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);
	additionalItems.add(bcastPortFieldLabel);
	additionalItems.add(bcastPortField);
	bcastPortFieldLabel.setLabelFor(bcastPortField);

    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter.
     */
    public ConnectionConfig() {
        super();
	additionalItems.add(bcastPortFieldLabel);
	additionalItems.add(bcastPortField);
	bcastPortFieldLabel.setLabelFor(bcastPortField);
    }

    @Override
    public String name() {
        return Bundle.getMessage("LenzLiusbServerName");
    }

    /**
     * Load the adapter with an appropriate object
     * <i>unless</i> it has already been set.
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new LIUSBServerAdapter();
        }
    }

    @Override
    public void loadDetails(JPanel details) {
        super.loadDetails(details);
        hostNameField.setText(LIUSBServerAdapter.DEFAULT_IP_ADDRESS);
        hostNameField.setEnabled(false); // we can't change this now.
        portFieldLabel.setText(Bundle.getMessage("CommunicationPortLabel"));
        portField.setText(String.valueOf(LIUSBServerAdapter.COMMUNICATION_TCP_PORT));
        portField.setEnabled(false); // we can't change this now.
        options.get(adapter.getOption1Name()).getComponent().setEnabled(false); // we can't change this now.
        bcastPortField.setEnabled(false); // we can't change this now.
    }

    protected JTextField bcastPortField = new JTextField(String.valueOf(LIUSBServerAdapter.BROADCAST_TCP_PORT));
    protected JLabel bcastPortFieldLabel = new JLabel(Bundle.getMessage("BroadcastPortLabel"));

}

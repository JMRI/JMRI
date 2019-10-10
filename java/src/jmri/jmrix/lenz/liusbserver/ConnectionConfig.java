package jmri.jmrix.lenz.liusbserver;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;

/**
 * Handle configuring an XpressNet layout connection via a LIUSB Server.
 * <p>
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
        return Bundle.getMessage("LenzLiusbServerName");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new LIUSBServerAdapter();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(JPanel details) {
        super.loadDetails(details);
        hostNameField.setText(LIUSBServerAdapter.DEFAULT_IP_ADDRESS);
        hostNameField.setEnabled(false); // we can't change this now.
        portFieldLabel.setText(Bundle.getMessage("CommunicationPortLabel"));
        portField.setText(String.valueOf(LIUSBServerAdapter.COMMUNICATION_TCP_PORT));
        portField.setEnabled(false); // we can't change this now.
        options.get(adapter.getOption1Name()).getComponent().setEnabled(false); // we can't change this now.
    }

}

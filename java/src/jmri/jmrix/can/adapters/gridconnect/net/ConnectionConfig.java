package jmri.jmrix.can.adapters.gridconnect.net;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.ConfigurationManager;
import jmri.jmrix.openlcb.swing.protocoloptions.ConfigPaneHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Definition of objects to handle configuring a connection via a
 * NetworkDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class ConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    public final static String NAME = "CAN via GridConnect Network Interface";

    /**
     * Create a connection configuration with a preexisting adapter. This is
     * used principally when loading a configuration that defines this
     * connection.
     *
     * @param p the adapter to create a connection configuration for
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(JPanel details) {
        setInstance();
        ConfigPaneHelper.maybeAddOpenLCBProtocolOptionsButton(this, additionalItems);
        super.loadDetails(details);
    }

    @Override
    public String name() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void checkInitDone() {
        log.debug("init called for {}", name());
        if (init) {
            return;
        }
        super.checkInitDone();

        updateUserNameField();

        ((JComboBox<Option>) options.get("Protocol").getComponent()).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateUserNameField();
            }
        });
    }

    void updateUserNameField() {
        if (!CanSystemConnectionMemo.DEFAULT_USERNAME.equals(adapter.getSystemConnectionMemo()
                .getUserName())) {
            // User name already set; do not overwrite it.
            log.debug("Avoid overwriting user name {}.", adapter.getSystemConnectionMemo()
                    .getUserName());
            return;
        }
        log.debug("New user name based on manufacturer {}", getManufacturer());
        String newUserName = getManufacturer();
        connectionNameField.setText(newUserName);

        if (!adapter.getSystemConnectionMemo().setUserName(newUserName)) {
            for (int x = 2; x < 50; x++) {
                if (adapter.getSystemConnectionMemo().setUserName(newUserName + x)) {
                    connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    break;
                }
            }
        }
    }

    /**
     * Access to current selected command station mode
     */
    /*public String getMode() {
     return opt2Box.getSelectedItem().toString();
     }*/
    @Override
    public boolean isPortAdvanced() {
        return false;
    }

    public boolean isOptList1Advanced() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new NetworkDriverAdapter();
        }
    }

    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.can.CanActionListBundle");
    }

    private final static Logger log = LoggerFactory.getLogger(ConnectionConfig.class);

}

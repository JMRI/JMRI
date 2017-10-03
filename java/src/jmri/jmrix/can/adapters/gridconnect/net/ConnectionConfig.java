package jmri.jmrix.can.adapters.gridconnect.net;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JComboBox;
import jmri.jmrix.can.ConfigurationManager;
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
        return NAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void checkInitDone() {
        if (log.isDebugEnabled()) {
            log.debug("init called for " + name());
        }
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
        String selection = options.get("Protocol").getItem();
        String newUserName = "MERG";
        if (ConfigurationManager.OPENLCB.equals(selection)) {
            newUserName = "OpenLCB";
        } else if (ConfigurationManager.RAWCAN.equals(selection)) {
            newUserName = "CANraw";
        } else if (ConfigurationManager.TEST.equals(selection)) {
            newUserName = "CANtest";
        }
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

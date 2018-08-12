package jmri.jmrix.can.adapters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JComboBox;
import jmri.jmrix.can.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for objects to handle configuring a layout connection via
 * various types of SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2012
 * @author Andrew Crosland 2008
 *
 */
abstract public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    private final static Logger log = LoggerFactory.getLogger(ConnectionConfig.class);

    /**
     * Create a connection configuration with a preexisting adapter. This is
     * used principally when loading a configuration that defines this
     * connection.
     *
     * @param p the adapter to create a connection configuration for
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Create a connection configuration without a preexisting adapter.
     */
    public ConnectionConfig() {
        super();
    }

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

    @Override
    abstract public String name();

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.can.CanActionListBundle");
    }

    @Override
    abstract protected void setInstance(); // necessary to get right type

}

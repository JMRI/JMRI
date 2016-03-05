// ConnectionConfig.java
package jmri.jmrix.can.adapters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JComboBox;
import jmri.jmrix.can.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for of objects to handle configuring a layout connection via
 * various types of SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2012
 * @author Andrew Crosland 2008
 * @version	$Revision: 19909 $
 */
abstract public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    private final static Logger log = LoggerFactory.getLogger(ConnectionConfig.class);
    
    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
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

    abstract public String name();

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.can.CanActionListBundle");
    }

    abstract protected void setInstance(); // necessary to get right type
}

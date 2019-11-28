package jmri.jmrix.grapevine.serialdriver;

import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JPanel;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.nodeconfig.NodeConfigAction;

/**
 * Definition of objects to handle configuring a Grapevine layout connection.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public ConnectionConfig() {
        super();
    }

    JButton b = new JButton(Bundle.getMessage("ConfigNodesTitle"));

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(JPanel details) {
        setInstance();

        // have to embed the usual one in a new JPanel
        b.addActionListener(new NodeConfigAction((GrapevineSystemConnectionMemo)adapter.getSystemConnectionMemo()));
        if (!additionalItems.contains(b)) {
            additionalItems.add(b);
        }
        super.loadDetails(details);
    }

    @Override
    public String name() {
        return Bundle.getMessage("GrapevineSerialName");
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.grapevine.GrapevineActionListBundle");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
           adapter = new SerialDriverAdapter();
        }
    }

}

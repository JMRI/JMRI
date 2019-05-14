package jmri.jmrix.acela.serialdriver;

import javax.swing.JButton;
import javax.swing.JPanel;
import jmri.jmrix.acela.nodeconfig.NodeConfigAction;

/**
 * Definition of objects to handle configuring a CTI Electronics Acela layout
 * connection via a SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2008
 * @author Bob Coleman, Copyright (C) 2007, 2008 Based on MRC example, modified
 * to establish Acela support.
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

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
        // have to embed the usual one in a new JPanel

        setInstance();

        b.addActionListener(new NodeConfigAction());
        if (!additionalItems.contains(b)) {
            additionalItems.add(b);
        }
        super.loadDetails(details);
    }

    @Override
    public String name() {
        return "Acela"; // NOI18N
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

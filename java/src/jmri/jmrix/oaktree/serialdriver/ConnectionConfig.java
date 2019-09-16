package jmri.jmrix.oaktree.serialdriver;

import javax.swing.JButton;
import javax.swing.JPanel;
import jmri.jmrix.oaktree.OakTreeSystemConnectionMemo;
import jmri.jmrix.oaktree.nodeconfig.NodeConfigAction;

/**
 * Definition of objects to handle configuring a Oak Tree layout connection.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006
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

        b.addActionListener(new NodeConfigAction((OakTreeSystemConnectionMemo) adapter.getSystemConnectionMemo()));
        // add another button
        if (!additionalItems.contains(b)) {
            additionalItems.add(b);
        }
        super.loadDetails(details);
    }

    @Override
    public String name() {
        return Bundle.getMessage("RciBus");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null ) {
           adapter = new SerialDriverAdapter();
        }
    }

}

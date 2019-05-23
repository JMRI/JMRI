package jmri.jmrix.ieee802154.serialdriver;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * Definition of objects to handle configuring a layout connection
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     *
     * @param p serial port adapter for connection
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(JPanel details) {
        // have to embed the usual one in a new JPanel

        JPanel p = new JPanel();
        super.loadDetails(p);

        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.add(p);

        // add another button
        //JButton b = new JButton("Configure nodes");
        //details.add(b);
        //b.addActionListener(new NodeConfigAction());  
    }

    @Override
    public String name() {
        return "Generic IEEE 802.15.4";
    }

    public boolean isOptList1Advanced() {
        return false;
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new SerialDriverAdapter();
        }
    }

}

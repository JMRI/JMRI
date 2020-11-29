package jmri.jmrix.powerline.serialdriver;

import javax.swing.JPanel;

/**
 * Definition of objects to handle configuring a Powerline layout connection.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     * @param p port adapter for connection
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
        super.loadDetails(details);

        // add another button
        //JButton b = new JButton("Configure nodes");
        //details.add(b);
        //b.addActionListener(new NodeConfigAction());  
    }

    @Override
    public String name() {
        return Bundle.getMessage("PlDeviceConnectionTitle");
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
            adapter = new SerialDriverAdapter();
        }
    }

}

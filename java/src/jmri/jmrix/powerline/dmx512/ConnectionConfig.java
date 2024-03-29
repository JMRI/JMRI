package jmri.jmrix.powerline.dmx512;

import javax.swing.JPanel;

/**
 * Definition of objects to handle configuring a layout connection
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @author Ken Cameron Copyright (C) 2023
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     *
     * @param p port adapter
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

    }

    @Override
    public String name() {
        return "DMX512"; // NOI18N
    }

    public boolean isOptList1Advanced() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new SpecificDriverAdapter();
        }
    }

}

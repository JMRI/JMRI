package jmri.jmrix.marklin.networkdriver;

import javax.swing.JPanel;

/**
 * Definition of objects to handle configuring an Marklin CS2 layout connection
 * via a NetworkDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 */
public class ConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
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
    public void loadDetails(final JPanel details) {
        super.loadDetails(details);
        portField.setEnabled(false);
    }

    @Override
    public String name() {
        return "CS2 via network";
    } // NOI18N

    /**
     * Access to current selected command station mode
     */
    /*public String getMode() {
     return opt2Box.getSelectedItem().toString();
     }*/

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new NetworkDriverAdapter();
        }
    }

}

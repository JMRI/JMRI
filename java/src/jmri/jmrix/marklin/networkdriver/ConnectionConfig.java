// ConnectionConfig.java

package jmri.jmrix.marklin.networkdriver;

import javax.swing.JPanel;



/**
 * Definition of objects to handle configuring an Marklin CS2 layout connection
 * via a NetworkDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 18902 $
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractNetworkConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p){
        super(p);
    }
    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public void loadDetails(final JPanel details) {
        super.loadDetails(details);
        portField.setEnabled(false);
    }
    
    public String name() { return "CS2 via network"; }

    /**
     * Access to current selected command station mode
     */
    /*public String getMode() {
        return opt2Box.getSelectedItem().toString();
    }*/
    
    protected void setInstance() {
        if (adapter==null){
            adapter = new NetworkDriverAdapter();
        }
    }
    
}


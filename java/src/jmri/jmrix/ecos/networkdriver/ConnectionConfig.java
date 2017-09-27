package jmri.jmrix.ecos.networkdriver;

//import javax.swing.*;
/**
 * Definition of objects to handle configuring an ECOS layout connection via a
 * NetworkDriverAdapter object.
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
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return Bundle.getMessage("EcosViaNetworkConnection");
    }

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
            adapter.setPort(15471);
        }
    }

}

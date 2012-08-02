// ConnectionConfig.java

package jmri.jmrix.can.adapters.gridconnect.net;

import java.util.ResourceBundle;

/**
 * Definition of objects to handle configuring a connection
 * via a NetworkDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @version	$Revision$
 */
 public class ConnectionConfig  extends jmri.jmrix.AbstractNetworkConnectionConfig {

	public final static String NAME = "CAN via GridConnect Network Interface";
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
    
    public String name() { return NAME; }

    /**
     * Access to current selected command station mode
     */
    /*public String getMode() {
        return opt2Box.getSelectedItem().toString();
    }*/
    
    public boolean isPortAdvanced() { return false; }
    public boolean isOptList1Advanced() { return false; }
    
    protected void setInstance() {
        if (adapter==null){
            adapter = new NetworkDriverAdapter();
        }
    }
    
    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.can.CanActionListBundle");
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectionConfig.class.getName());
}


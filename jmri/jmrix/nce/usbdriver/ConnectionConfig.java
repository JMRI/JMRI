// ConnectionConfig.java

package jmri.jmrix.nce.usbdriver;


/**
 * Definition of objects to handle configuring an LocoBuffer layout connection
 * via an NCE SerialDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @author 		Daniel Boudreau Copyright (C) 2007
 * @version	$Revision: 1.5 $
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractSerialConnectionConfig {

	public final static String NAME = "NCE USB";
	
    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        super(p);
    }
    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    public String name() { return NAME; }
    
    public boolean isOptList1Advanced() { return false; }

    protected void setInstance() { 
        if (adapter == null){
            adapter = UsbDriverAdapter.instance(); 
        }
    }
}


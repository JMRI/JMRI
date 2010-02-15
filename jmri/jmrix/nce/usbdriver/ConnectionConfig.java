// ConnectionConfig.java

package jmri.jmrix.nce.usbdriver;


/**
 * Definition of objects to handle configuring an LocoBuffer layout connection
 * via an NCE SerialDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @author 		Daniel Boudreau Copyright (C) 2007
 * @version	$Revision: 1.3 $
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractConnectionConfig {

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
    
    String manufacturerName = "NCE";
    
    public String getManufacturer() { return manufacturerName; }
    public void setManufacturer(String manu) { manufacturerName=manu; }
    
    public boolean isOptList1Advanced() { return false; }

    protected void setInstance() { adapter = UsbDriverAdapter.instance(); }
}


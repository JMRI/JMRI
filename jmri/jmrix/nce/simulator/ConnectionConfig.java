// ConnectionConfig.java

package jmri.jmrix.nce.simulator;

import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * Definition of objects to handle configuring a layout connection
 * via an NCE SerialDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.1 $
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractConnectionConfig {

	public final static String NAME = "Simulator";
	
    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        super(p);
    }
    /**
     * Ctor for a functional Swing object with no preexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    public String name() { return NAME; }
    
    public void loadDetails(JPanel details) {
        details.add(new JLabel("No options"));
    }
    
    String manufacturerName = jmri.jmrix.DCCManufacturerList.NCE;
    
    public String getManufacturer() { return manufacturerName; }
    public void setManufacturer(String manu) { manufacturerName=manu; }


    protected void setInstance() { adapter = SimulatorAdapter.instance(); }
}


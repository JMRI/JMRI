// ConnectionConfig.java

package jmri.jmrix.cmri.serial.sim;

import javax.swing.*;
import jmri.jmrix.cmri.serial.nodeconfigmanager.NodeConfigManagerAction;  //c2
//import jmri.jmrix.cmri.serial.nodeconfig.NodeConfigAction;

/**
 * Definition of objects to handle configuring a layout connection
 * via an CMRInet Simulator object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003, 2008
 * @version	$Revision: 20084 $
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractSimulatorConnectionConfig {

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

    public void loadDetails(JPanel details) {
    	// have to embed the usual one in a new JPanel
    	
    	JPanel p = new JPanel();
        super.loadDetails(p);

		details.setLayout(new BoxLayout(details,BoxLayout.Y_AXIS));
		details.add(p);

		// add another button
		JButton b = new JButton("Configure CMRInet nodes");

		details.add(b);
						
		b.addActionListener(new NodeConfigManagerAction());  //c2
//		b.addActionListener(new NodeConfigAction());
        
    }
    
	/*protected Vector<String> getPortNames() {
        Vector<String> portNameVector = new Vector<String>();
        portNameVector.addElement("(None)");
        return portNameVector;
    }*/

    //public boolean isPortAdvanced() { return true; }
    
    public String name() { return "Simulator"; }

    protected void setInstance() { adapter = SimDriverAdapter.instance(); }
}


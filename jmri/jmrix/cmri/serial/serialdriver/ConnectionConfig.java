// ConnectionConfig.java

package jmri.jmrix.cmri.serial.serialdriver;

import javax.swing.*;
import jmri.jmrix.cmri.serial.nodeconfig.NodeConfigAction;

/**
 * Definition of objects to handle configuring an LocoBuffer layout connection
 * via an C/MRI SerialDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.4 $
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractConnectionConfig {

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
		JButton b = new JButton("Configure C/MRI nodes");

		details.add(b);
						
		b.addActionListener(new NodeConfigAction());		
        
    }

	JFrame frame;
	
    public String name() { return "C/MRI"; }

    protected void setInstance() { adapter = SerialDriverAdapter.instance(); }
}


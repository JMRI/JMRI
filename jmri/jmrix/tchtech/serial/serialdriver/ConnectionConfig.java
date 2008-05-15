/*
 * ConnectionConfig.java
 *
 * Created on August 18, 2007, 10:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmri.jmrix.tchtech.serial.serialdriver;

import javax.swing.*;
import jmri.jmrix.tchtech.serial.nodeconfig.NodeConfigAction;

/**
 * Definition of objects to handle configuring an LocoBuffer layout connection
 * via an  SerialDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.1 $
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
		JButton b = new JButton("Provision Node Interface Cards");

		details.add(b);
						
		b.addActionListener(new NodeConfigAction());		
        
    }

	JFrame frame;
	
    public String name() { return "TCH Technology"; }

    protected void setInstance() { adapter = SerialDriverAdapter.instance(); }
}


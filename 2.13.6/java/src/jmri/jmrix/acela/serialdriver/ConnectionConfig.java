// ConnectionConfig.java

package jmri.jmrix.acela.serialdriver;

import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import jmri.jmrix.acela.nodeconfig.NodeConfigAction;

/**
 * Definition of objects to handle configuring an LocoBuffer layout connection
 * via an NCE SerialDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003, 2008
 * @version	$Revision$
 *
 * @author	Bob Coleman, Copyright (C) 2007, 2008
 *              Based on MRC example, modified to establish Acela support. 
 */

public class ConnectionConfig  extends jmri.jmrix.AbstractSerialConnectionConfig {

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
		JButton b = new JButton("Configure nodes");

		details.add(b);
						
		b.addActionListener(new NodeConfigAction());		
        
    }

    public String name() { return "Acela"; }

    protected void setInstance() {
        if (adapter == null){
            adapter = new SerialDriverAdapter();
        } 
    }
}

/* @(#)ConnectionConfig.java */
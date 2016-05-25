// ConnectionConfig.java
package jmri.jmrix.cmri.serial.serialdriver;

import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
// import jmri.jmrix.cmri.serial.nodeconfig.NodeConfigAction;
import jmri.jmrix.cmri.serial.nodeconfigmanager.NodeConfigManagerAction;

/**
 * Definition of objects to handle configuring a layout connection via an C/MRI
 * SerialDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @author	Chuck Catania  Copyright (C) 2014
 * @version	$Revision: 18323 $
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
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
						
//		b.addActionListener(new NodeConfigAction());  //c2		
//		b.addActionListener(new NodeTableAction());		
		b.addActionListener(new NodeConfigManagerAction());		
        
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.cmri.CmriActionListBundle");
    }

    public String name() {
        return "Serial";
    }

    protected void setInstance() {
        adapter = SerialDriverAdapter.instance();
    }
}

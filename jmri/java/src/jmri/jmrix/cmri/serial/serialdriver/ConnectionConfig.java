// ConnectionConfig.java

package jmri.jmrix.cmri.serial.serialdriver;

import javax.swing.*;
import java.util.ResourceBundle;

import jmri.jmrix.cmri.serial.nodeconfig.NodeConfigAction;

/**
 * Definition of objects to handle configuring a layout connection
 * via an C/MRI SerialDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision$
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
    
    JButton b = new JButton("Configure C/MRI nodes");

    public void loadDetails(JPanel details) {
    	
        b.addActionListener(new NodeConfigAction());
        if(!additionalItems.contains(b))
            additionalItems.add(b);
        super.loadDetails(details);
        
    }
    
    @Override
    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.cmri.CmriActionListBundle");
    }

    public String name() { return "Serial"; }

    protected void setInstance() { adapter = SerialDriverAdapter.instance(); }
}


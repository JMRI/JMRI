package jmri.jmrix.cmri.serial.serialdriver;

import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import jmri.jmrix.cmri.serial.nodeconfig.NodeConfigAction;
import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/**
 * Definition of objects to handle configuring a layout connection via an C/MRI
 * SerialDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @author	Chuck Catania  Copyright (C) 2014
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

    JButton b;

    @Override
    public void loadDetails(JPanel details) {
    	// have to embed the usual one in a new JPanel
    	
    	JPanel p = new JPanel();
        super.loadDetails(p);
        
        details.setLayout(new BoxLayout(details,BoxLayout.Y_AXIS));
        details.add(p);

        // add another button
        JButton b = new JButton("Configure CMRInet nodes");
        details.add(b);

        setInstance();
        b = new JButton("Configure C/MRI nodes");
        b.addActionListener(new NodeConfigAction((CMRISystemConnectionMemo)adapter.getSystemConnectionMemo()));
        if (!additionalItems.contains(b)) {
            additionalItems.add(b);
        }
        super.loadDetails(details);

    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.cmri.CmriActionListBundle");
    }

    @Override
    public String name() {
        return "Serial";
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new SerialDriverAdapter();
        }
    }
}

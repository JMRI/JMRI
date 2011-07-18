// ConnectionConfig.java

package jmri.jmrix.loconet.locormi;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.jmrix.JmrixConfigPane;

/**
 * Definition of objects to handle configuring  the layout connection
 * via LocoNet RMI.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision$
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(String p, String m){
        super();
        hostName = p;
        if (m!=null) manufacturerName = m;
    }
    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    public JTextField host;
    String hostName ="";

    public String name() { return "LocoNet Server"; }

    public void loadDetails(JPanel details) {
        details.setLayout(new BoxLayout(details, BoxLayout.X_AXIS));
        details.add(new JLabel("Server hostname:"));
        host = new JTextField(hostName);
        details.add(host);
    }

    /**
     * Reimplement this method to show the connected host,
     * rather than the usual port name.
     * <P>
     * If the connection isn't active, that's also indicated.
     * <P>
     * Do not use this to record the port name, as it also
     * contains that error information.
     *
     * @return Human-readable connection information
     */
    public String getInfo() {
        String t = host.getText();
        if (t != null && !t.equals("")) return t;
        else return JmrixConfigPane.NONE;
    }
    
    public boolean isOptList2Advanced() { return false; }
    
    protected void setInstance() {
        log.error("Unexpected call to setInstance");
        new Exception().printStackTrace();
    }
    
    String manufacturerName = jmri.jmrix.DCCManufacturerList.DIGITRAX;
    
    public String getManufacturer() { return manufacturerName; }
    public void setManufacturer(String manu) { manufacturerName=manu; }
}


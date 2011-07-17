// ConnectionConfig.java

package jmri.jmrix.can.adapters.gridconnect.net;

import javax.swing.*;

import jmri.jmrix.JmrixConfigPane;

/**
 * Definition of objects to handle configuring a connection
 * via a NetworkDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @version	$Revision: 1.2 $
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractSerialConnectionConfig {

	public final static String NAME = "CAN via GridConnect Network Interface";
    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(String h, String p, String m){
        super();
        hostName = h;
        portNumber = p;
        mode = m;
    }
    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }


    /**
     * set up all of the other objects to operate with a CAN net adapter
     * connected to this port
     */
    public void configure() {

        // Register the CAN traffic controller being used for this connection
        //GcTrafficController.instance();
        
        // Now connect to the traffic controller
        //log.debug("Connecting port");
        //GcTrafficController.instance().connectPort(adapter);

        // do central protocol-specific configuration    
        //jmri.jmrix.can.ConfigurationManager.configure("OpenLCB");

    }
    

    public JTextField host;
    String hostName ="";
    public JTextField port;
    String portNumber ="";

    String mode = "";
    
    public String name() { return NAME; }
    String manufacturerName = jmri.jmrix.DCCManufacturerList.OPENLCB;
    
    public String getManufacturer() { return manufacturerName; }
    public void setManufacturer(String manu) { manufacturerName=manu; }

    public void loadDetails(JPanel details) {
        JPanel temp = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        temp.setLayout(new BoxLayout(temp, BoxLayout.X_AXIS));
        temp.add(new JLabel("Server hostname:"));
        host = new JTextField(hostName);
        temp.add(host);
        details.add(temp);
        temp = new JPanel();
        temp.setLayout(new BoxLayout(temp, BoxLayout.X_AXIS));
        temp.add(new JLabel("Port number:"));
        port = new JTextField(portNumber);
        temp.add(port);
        details.add(temp);

        if (adapter == null) adapter = NetworkDriverAdapter.instance();
        String[] opt2List = adapter.validOption2();
        opt2Box.removeAllItems();
        for (int i=0; i<opt2List.length; i++) opt2Box.addItem(opt2List[i]);
        temp = new JPanel();
        temp.setLayout(new BoxLayout(temp, BoxLayout.X_AXIS));
        if (opt2List.length>1) {
            temp.add(new JLabel(adapter.option2Name()));
            temp.add(opt2Box);
            opt2Box.setSelectedItem(adapter.getCurrentOption2Setting());
            if (! mode.equals("")) opt2Box.setSelectedItem(mode);
        }
        details.add(temp);
    }

    /**
     * Access to current selected command station mode
     */
    public String getMode() {
        return opt2Box.getSelectedItem().toString();
    }
    
    /**
     * Reimplement this method to show the connected host,
     * rather than the usual port name.
     * @return human-readable connection information
     */
    public String getInfo() {
        String t = host.getText();
        if (t != null && !t.equals("")) return t;
        else return JmrixConfigPane.NONE;
    }
    protected void setInstance() {
        log.error("Unexpected call to setInstance");
        new Exception().printStackTrace();
    }
}


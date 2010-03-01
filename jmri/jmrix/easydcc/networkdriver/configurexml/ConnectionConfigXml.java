package jmri.jmrix.easydcc.networkdriver.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.easydcc.networkdriver.ConnectionConfig;
import jmri.jmrix.easydcc.networkdriver.NetworkDriverAdapter;

import org.jdom.*;
import javax.swing.*;

/**
 * Handle XML persistance of layout connections by persistening
 * the NetworkDriverAdapter (and connections).
 * <P>
 * Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the NetworkDriverAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.8 $
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    protected void getInstance() {
        log.error("unexpected call to getInstance");
        new Exception().printStackTrace();
    }

    public Element store(Object o) {
        ConnectionConfig c = (ConnectionConfig)o;
        Element e = new Element("connection");
        e.setAttribute("manufacturer", c.getManufacturer());
        e.setAttribute("port", c.host.getText());
        e.setAttribute("option1", c.port.getText());

        e.setAttribute("class", this.getClass().getName());

        return e;
    }
    /**
     * Port name carries the hostname for the network connection
     * @param e Top level Element to unpack.
     * @return true if successful
      */
    public boolean load(Element e) {
    	boolean result = true;
        // configure port name
        String hostName = e.getAttribute("port").getValue();
        String portNumber = e.getAttribute("option1").getValue();

        // notify
        JFrame f = new JFrame("EasyDCC network connection");
        f.getContentPane().add(new JLabel("Connecting to "+hostName+":"+portNumber));
        f.pack();
        f.setVisible(true);

        // slightly different, as not based on a serial port...
        // create the adapter
        NetworkDriverAdapter client = new NetworkDriverAdapter();

        // start the connection
        try {
            client.connect(hostName, Integer.parseInt(portNumber));
        } catch (Exception ex) {
            log.error("Error opening connection to "+hostName+" was: "+ex);
            result = false;
        }
        
        String manufacturer = null;
        try { 
            manufacturer = e.getAttribute("manufacturer").getValue();
        } catch ( NullPointerException ex) { //Considered normal if not present
        }

        // configure the other instance objects
        client.configure();

        f.setVisible(false);
        f.dispose();

        // register, so can be picked up
        register(hostName, portNumber, manufacturer);
        return result;
    }

    protected void register() {
        log.error("unexpected call to register()");
        new Exception().printStackTrace();
    }
    protected void register(String host, String port, String manufacturer) {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(host, port, manufacturer));
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectionConfigXml.class.getName());

}
package jmri.jmrix.can.adapters.gridconnect.net.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;

import jmri.jmrix.can.adapters.gridconnect.net.ConnectionConfig;
import jmri.jmrix.can.adapters.gridconnect.net.NetworkDriverAdapter;

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
 * @version $Revision: 1.1 $
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

        e.setAttribute("port", c.host.getText());
        e.setAttribute("option1", c.port.getText());
        e.setAttribute("option2", c.getMode());

        e.setAttribute("class", this.getClass().getName());

        return e;
    }
    /**
     * Port name carries the hostname for the network connection
     * @param e Top level Element to unpack.
     * @return true if successul
      */
    public boolean load(Element e) {
    	boolean result = true;
        // configure port name
        String hostName = e.getAttribute("port").getValue();
        String portNumber = e.getAttribute("option1").getValue();
        String mode = "";
        if (e.getAttribute("option2") != null) {
            mode = e.getAttribute("option2").getValue();
        }
        
        // notify
        JFrame f = new JFrame("Network connection");
        f.getContentPane().add(new JLabel("Connecting to "+hostName+":"+portNumber));
        f.pack();
        f.setVisible(true);

        // slightly different, as not based on a serial port...
        // create the adapter
        NetworkDriverAdapter client = new NetworkDriverAdapter();
        
        // load configuration
        client.configureOption2(mode);
        client.setHostName(hostName);

        // start the connection
        try {
            client.connect(hostName, Integer.parseInt(portNumber));
        } catch (Exception ex) {
            log.error("Error opening connection to "+hostName+" was: "+ex);
            result = false;
        }

        // configure the other instance objects
        client.configure();
        // if successful so far, go ahead and configure

        f.setVisible(false);
        f.dispose();

        // register, so can be picked up
        register(hostName, portNumber, mode);
        return result;
    }

    protected void register() {
        log.error("unexpected call to register()");
        new Exception().printStackTrace();
    }
    protected void register(String host, String port, String mode) {
        log.debug("expected call to register()", new Exception());
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(host, port, mode));
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectionConfigXml.class.getName());

}
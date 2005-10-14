package jmri.jmrix.loconet.locormi.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.loconet.locormi.ConnectionConfig;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.jdom.Element;

/**
 * Handle XML persistance of layout connections by persistening
 * the RMI objects (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the RMI info.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.5 $
 */
public class ConnectionConfigXml extends AbstractConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    protected void getInstance() {
        log.error("unexpected call to getInstance");
        new Exception().printStackTrace();
    }

    public Element store(Object o) {
        jmri.jmrix.loconet.locormi.ConnectionConfig c = (jmri.jmrix.loconet.locormi.ConnectionConfig)o;
        Element e = new Element("connection");

        e.addAttribute("port", c.host.getText());

        e.addAttribute("class", this.getClass().getName());

        return e;
    }
    /**
     * Port name carries the hostname for the RMI connection
     * @param e Top level Element to unpack.
      */
    public void load(Element e) {
        // configure port name
        String hostName = e.getAttribute("port").getValue();

        // notify
        JFrame f = new JFrame("LocoNet server connection");
        f.getContentPane().add(new JLabel("Connecting to "+hostName));
        f.pack();
        f.setVisible(true);

        // slightly different, as not based on a serial port...
        // create the LnMessageClient
        jmri.jmrix.loconet.locormi.LnMessageClient client = new jmri.jmrix.loconet.locormi.LnMessageClient();

        // start the connection
        try {
            client.configureRemoteConnection(hostName, 500);
            connected = true;   // exception during connect skips this
        } catch (jmri.jmrix.loconet.LocoNetException ex) {
            log.error("Error opening connection to "+hostName+" was: "+ex);
            f.setTitle("Server connection failed");
            f.getContentPane().removeAll();
            f.getContentPane().add(new JLabel("failed, error was "+ex));
            f.pack();
            connected = false;
        }

        // configure the other instance objects
        client.configureLocalServices();

        if (connected) {
            f.setVisible(false);
            f.dispose();
        }

        // register, so can be picked up
        register(hostName);
    }

    boolean connected = false;

    protected void register() {
        log.error("unexpected call to register()");
        new Exception().printStackTrace();
    }
    protected void register(String host) {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(host));
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConnectionConfigXml.class.getName());

}
package jmri.jmrix.loconet.loconetovertcp.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.loconet.loconetovertcp.ConnectionConfig;
import jmri.jmrix.loconet.loconetovertcp.LnTcpDriverAdapter;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.jdom.Element;

/**
 * Handle XML persistance of layout connections by persistening
 * the LnTcpDriverAdapter (and connections).
 * <P>
 * Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the LnTcpDriverAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.2 $
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
        ConnectionConfig c = (ConnectionConfig)o;
        Element e = new Element("connection");

        e.addAttribute("port", c.host.getText());
        e.addAttribute("option1", c.port.getText());

        e.addAttribute("class", this.getClass().getName());

        return e;
    }
    /**
     * Port name carries the hostname for the network connection
     * @param element Top level Element to unpack.
      */
    public void load(Element e) {
        // configure port name
        String hostName = e.getAttribute("port").getValue();
        String portNumber = e.getAttribute("option1").getValue();

        // notify
        JFrame f = new JFrame("LocoNetOverTcp network connection");
        f.getContentPane().add(new JLabel("Connecting to "+hostName+":"+portNumber));
        f.pack();
        f.show();

        // slightly different, as not based on a serial port...
        // create the adapter
        LnTcpDriverAdapter client = new LnTcpDriverAdapter();

        // start the connection
        try {
            client.connect(hostName, Integer.parseInt(portNumber));
        } catch (Exception ex) {
            log.error("Error opening connection to "+hostName+" was: "+ex);
        }

        // configure the other instance objects
        client.configure();

        f.hide();
        f.dispose();

        // register, so can be picked up
        register(hostName, portNumber);
    }

    protected void register() {
        log.error("unexpected call to register()");
        new Exception().printStackTrace();
    }
    protected void register(String host, String port) {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(host, port));
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConnectionConfigXml.class.getName());

}
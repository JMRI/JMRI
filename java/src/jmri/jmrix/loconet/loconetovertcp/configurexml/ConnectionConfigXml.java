package jmri.jmrix.loconet.loconetovertcp.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
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
 * @version $Revision$
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    protected void getInstance() {
        log.error("unexpected call to getInstance");
        new Exception().printStackTrace();
    }
    
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig)object).getAdapter();
    }

    public Element store(Object o) {
        getInstance(o);
        ConnectionConfig c = (ConnectionConfig)o;
        Element e = new Element("connection");
        if(adapter!=null){
            if (adapter.getSystemConnectionMemo()!=null){
                e.setAttribute("userName", adapter.getSystemConnectionMemo().getUserName());
                e.setAttribute("systemPrefix", adapter.getSystemConnectionMemo().getSystemPrefix());
            }
            if (adapter.getManufacturer()!=null)
                e.setAttribute("manufacturer", adapter.getManufacturer());
        }

        e.setAttribute("manufacturer", c.getManufacturer());
        e.setAttribute("class", this.getClass().getName());
        e.setAttribute("hostname",c.host.getText());
        e.setAttribute("port",c.port.getText());
        e.setAttribute("cmd-station",(String) c.commandStation.getSelectedItem());

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
        String hostName   = e.getAttribute("hostname").getValue();
        String portNumber = e.getAttribute("port").getValue();
        String cmdStation = e.getAttribute("cmd-station").getValue();
        String manufacturer = null;
        try { 
            manufacturer = e.getAttribute("manufacturer").getValue();
        } catch ( NullPointerException ex) { //Considered normal if not present
        }
        // notify
        JFrame f = new JFrame("LocoNetOverTcp network connection");
        f.getContentPane().add(new JLabel("Connecting to "+hostName+":"+portNumber));
        f.pack();
        f.setVisible(true);
        // slightly different, as not based on a serial port...
        // create the adapter
        LnTcpDriverAdapter client = LnTcpDriverAdapter.instance();
        //adapter = f.getAdapter();
        if(adapter!=null){
            try { 
                manufacturer = e.getAttribute("manufacturer").getValue();
                client.setManufacturer(manufacturer);
            } catch ( NullPointerException ex) { //Considered normal if not present
                
            }
            if (adapter.getSystemConnectionMemo()!=null){
                if (e.getAttribute("userName")!=null) {
                    client.getSystemConnectionMemo().setUserName(e.getAttribute("userName").getValue());
                }
                
                if (e.getAttribute("systemPrefix")!=null) {
                    client.getSystemConnectionMemo().setSystemPrefix(e.getAttribute("systemPrefix").getValue());
                }
            }
        }
        register(hostName, portNumber, manufacturer);
        // start the connection
        try {
            client.connect(hostName, Integer.parseInt(portNumber));
        } catch (Exception ex) {
            log.error("Error opening connection to "+hostName+" was: "+ex);
            jmri.configurexml.ConfigXmlManager.creationErrorEncountered(
                                        null, "opening connection",
                                        org.apache.log4j.Level.ERROR,
                                        ex.getMessage(),
                                        null,null,null
                                    );
            return false;
        }

        // configure the other instance objects
        client.setCommandStationType(cmdStation);
        client.configure();

        f.setVisible(false);
        f.dispose();

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

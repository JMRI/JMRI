package jmri.jmrix.loconet.locormi.configurexml;

import javax.swing.JFrame;
import javax.swing.JLabel;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.loconet.locormi.ConnectionConfig;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of layout connections by persistening the RMI objects
 * (and connections). Note this is named as the XML version of a
 * ConnectionConfig object, but it's actually persisting the RMI info.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
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

    public Element store(Object o) {
        jmri.jmrix.loconet.locormi.ConnectionConfig c = (jmri.jmrix.loconet.locormi.ConnectionConfig) o;
        Element e = new Element("connection");
        e.setAttribute("manufacturer", c.getManufacturer());
        e.setAttribute("port", c.host.getText());

        if (c.getLnMessageClient() != null) {
            if (c.getLnMessageClient().getAdapterMemo() != null) {
                e.setAttribute("userName", c.getLnMessageClient().getAdapterMemo().getUserName());
                e.setAttribute("systemPrefix", c.getLnMessageClient().getAdapterMemo().getSystemPrefix());
            }
            if (c.getDisabled()) {
                e.setAttribute("disabled", "yes");
            } else {
                e.setAttribute("disabled", "no");
            }
        }

        e.setAttribute("class", this.getClass().getName());

        return e;
    }

    /**
     * Port name carries the hostname for the RMI connection
     *
     * @param shared Top level Element to unpack.
     * @param perNode
     * @return true if successful
     */
    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        // configure port name
        String hostName = shared.getAttribute("port").getValue();
        String manufacturer = null;

        try {
            manufacturer = shared.getAttribute("manufacturer").getValue();
        } catch (NullPointerException ex) { //Considered normal if not present
        }

        ConnectionConfig cc = new ConnectionConfig(hostName, manufacturer);
        jmri.jmrix.loconet.locormi.LnMessageClient client = new jmri.jmrix.loconet.locormi.LnMessageClient();
        cc.setLnMessageClient(client);

        if (shared.getAttribute("disabled") != null) {
            String yesno = shared.getAttribute("disabled").getValue();
            if ((yesno != null) && (!yesno.equals(""))) {
                if (yesno.equals("no")) {
                    cc.setDisabled(false);
                } else if (yesno.equals("yes")) {
                    cc.setDisabled(true);
                }
            }
        }

        if (client.getAdapterMemo() != null) {
            if (shared.getAttribute("userName") != null) {
                client.getAdapterMemo().setUserName(shared.getAttribute("userName").getValue());
            }

            if (shared.getAttribute("systemPrefix") != null) {
                client.getAdapterMemo().setSystemPrefix(shared.getAttribute("systemPrefix").getValue());
            }
        }

        if (!cc.getDisabled()) {
            // notify
            JFrame f = new JFrame("LocoNet server connection");
            f.getContentPane().add(new JLabel("Connecting to " + hostName));
            f.pack();
            f.setVisible(true);

            // slightly different, as not based on a serial port...
            // create the LnMessageClient
            // start the connection
            try {
                client.configureRemoteConnection(hostName, 500);
                connected = true;   // exception during connect skips this
            } catch (jmri.jmrix.loconet.LocoNetException ex) {
                log.error("Error opening connection to " + hostName + " was: " + ex);
                f.setTitle("Server connection failed");
                f.getContentPane().removeAll();
                f.getContentPane().add(new JLabel("failed, error was " + ex));
                f.pack();
                jmri.jmrix.ConnectionStatus.instance().setConnectionState(cc.getInfo(), jmri.jmrix.ConnectionStatus.CONNECTION_DOWN);
                connected = false;
                result = false;
            }

            if (connected) {
                jmri.jmrix.ConnectionStatus.instance().setConnectionState(cc.getInfo(), jmri.jmrix.ConnectionStatus.CONNECTION_UP);
                // configure the other instance objects only if connected.
                client.configureLocalServices();
                f.setVisible(false);
                f.dispose();
            }
        }

        // register, so can be picked up
        register(cc);
        return result;
    }

    boolean connected = false;

    protected void register() {
        log.error("unexpected call to register()");
        new Exception().printStackTrace();
    }

    protected void register(ConnectionConfig cc) {
        super.register(cc);
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class.getName());

}

package jmri.jmrix.loconet.locormi.configurexml;

import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notification of removal of LocoNet RMI connection and replacement with
 * LocoNetOverTCP connection.
 *
 * @deprecated since 4.7.4 without direct replacement
 * @author Randall Wood (C) 2017
 */
@Deprecated
public class ConnectionConfigXml extends jmri.jmrix.loconet.loconetovertcp.configurexml.ConnectionConfigXml {

    private final static Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class);

    public ConnectionConfigXml() {
        super();
    }

    @Override
    public boolean load(Element shared, Element perNode) throws Exception {
        if (perNode == null) {
            perNode = shared;
        }
        perNode.setAttribute("address", perNode.getAttributeValue("port"));
        perNode.setAttribute("port", "1234"); // use default port
        super.load(shared, perNode);
        log.error("The LocoNet RMI connection is no longer supported.");
        log.error("This connection will be converted to a LocoNetOverTcp connection.");
        log.error("See the JMRI 4.7.4 release notes for more information.");
        // avoid the class overhead of the standard Bundle construct
        ResourceBundle bundle = ResourceBundle.getBundle("jmri.jmrix.loconet.locormi.configurexml.Bundle");
        if (!GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(null,
                    bundle.getString("NoLocoRmiMessage"),
                    bundle.getString("NoLocoRmiTitle"),
                    JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
}

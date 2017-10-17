package jmri.jmrix.ieee802154.xbee.configurexml;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import java.util.List;
import jmri.jmrix.AbstractStreamPortController;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.ieee802154.xbee.ConnectionConfig;
import jmri.jmrix.ieee802154.xbee.XBeeAdapter;
import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import jmri.jmrix.ieee802154.xbee.XBeeNode;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of layout connections by persisting the XBeeAdapter
 * (and connections). Note this is named as the XML version of a
 * ConnectionConfig object, but it's actually persisting the XBeeAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2006, 2007, 2008
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    /**
     * Write out the SerialNode objects too
     *
     * @param e Element being extended
     */
    @Override
    protected void extendElement(Element e) {
        XBeeConnectionMemo xcm;
        XBeeTrafficController xtc;
        try {
            xcm = (XBeeConnectionMemo) adapter.getSystemConnectionMemo();
            xtc = (XBeeTrafficController) xcm.getTrafficController();
        } catch (NullPointerException npe) {
            // The adapter doesn't have a memo, so no nodes can be defined.
            if (log.isDebugEnabled()) {
                log.debug("No memo defined; no nodes to save.");
            }
            return;
        }
        try {
            XBeeNode node = (XBeeNode) xtc.getNode(0);
            int index = 1;
            while (node != null) {
                // add node as an element
                Element n = new Element("node");
                n.setAttribute("name", "" + node.getNodeAddress());
                e.addContent(n);
                // add parameters to the node as needed
                n.addContent(makeParameter("address", ""
                        + jmri.util.StringUtil.hexStringFromBytes(node.getUserAddress())));
                n.addContent(makeParameter("PAN", ""
                        + jmri.util.StringUtil.hexStringFromBytes(node.getPANAddress())));
                n.addContent(makeParameter("GUID", ""
                        + jmri.util.StringUtil.hexStringFromBytes(node.getGlobalAddress())));
                n.addContent(makeParameter("name", node.getIdentifier()));
                n.addContent(makeParameter("polled", node.getPoll() ? "yes" : "no"));

                jmri.jmrix.AbstractStreamPortController pc = null;
                if ((pc = node.getPortController()) != null) {
                    n.addContent(makeParameter("StreamController",
                            pc.getClass().getName()));
                }

                // look for the next node
                node = (XBeeNode) xtc.getNode(index);
                index++;
            }
        } catch (java.lang.NullPointerException npe2) {
            // no nodes defined.
            return;
        }
    }

    protected Element makeParameter(String name, String value) {
        Element p = new Element("parameter");
        p.setAttribute("name", name);
        p.addContent(value);
        return p;
    }

    @Override
    protected void getInstance() {
        adapter = new XBeeAdapter();
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void unpackElement(Element shared, Element perNode) {
        List<Element> l = shared.getChildren("node");
        // Trigger initialization of this Node to reflect these parameters
        XBeeConnectionMemo xcm = (XBeeConnectionMemo) adapter.getSystemConnectionMemo();
        XBeeTrafficController xtc = (XBeeTrafficController) xcm.getTrafficController();
        for (int i = 0; i < l.size(); i++) {
            Element n = l.get(i);
            byte GUID[] = jmri.util.StringUtil.bytesFromHexString(findParmValue(n, "GUID"));
            XBee64BitAddress guid = new XBee64BitAddress(GUID);
            byte addr[] = jmri.util.StringUtil.bytesFromHexString(findParmValue(n, "address"));
            XBee16BitAddress address = new XBee16BitAddress(addr);
            String Identifier = findParmValue(n, "name");
            // create the RemoteXBeeDevice for the node.
            RemoteXBeeDevice remoteDevice = new RemoteXBeeDevice(xtc.getXBee(),
                         guid,address,Identifier);
            // Check to see if the node is a duplicate, if it is, move 
            // to the next one.
            // get a XBeeNode corresponding to this node address if one exists
           XBeeNode curNode = (XBeeNode) xtc.getNodeFromXBeeDevice(remoteDevice);
           if (curNode != null) {
               log.info("Read duplicate node {} from file",remoteDevice);
               continue; 
           }
    
            try {
               // and then add it to the network
               xtc.getXBee().getNetwork().addRemoteDevice(remoteDevice);
               // create node (they register themselves)
               XBeeNode node = new XBeeNode(remoteDevice);
            
               String polled = findParmValue(n, "polled");
               node.setPoll(polled.equals("yes"));
                     
               xtc.registerNode(node);

               // if there is a stream port controller stored for this
               // node, we need to load that after the node starts running.
               // otherwise, the IOStream associated with the node has not
               // been configured.
               String streamController = findParmValue(n, "StreamController");
               if (streamController != null) {
                    try {
                        @SuppressWarnings("unchecked") // Class.forName cast is unchecked at this point
                        java.lang.Class<jmri.jmrix.AbstractStreamPortController> T = (Class<AbstractStreamPortController>) Class.forName(streamController);
                        node.connectPortController(T);
                    } catch (java.lang.ClassNotFoundException cnfe) {
                        log.error("Unable to find class for stream controller : {}",streamController);
                    }
                }
            } catch (TimeoutException toe) {
               log.error("Timeout adding node {} from configuration file.",remoteDevice);
            } catch (XBeeException xbe) {
               log.error("Exception adding node {} from configuration file.",remoteDevice);
            }

        }
    }

    /**
     * Service routine to look through "parameter" child elements to find a
     * particular parameter value
     *
     * @param e    Element containing parameters
     * @param name name of desired parameter
     * @return String value
     */
    String findParmValue(Element e, String name) {
        List<Element> l = e.getChildren("parameter");
        for (int i = 0; i < l.size(); i++) {
            Element n = l.get(i);
            if (n.getAttributeValue("name").equals(name)) {
                return n.getTextTrim();
            }
        }
        return null;
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class);

}

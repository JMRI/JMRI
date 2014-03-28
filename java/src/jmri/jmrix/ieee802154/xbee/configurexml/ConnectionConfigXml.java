package jmri.jmrix.ieee802154.xbee.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.ieee802154.xbee.ConnectionConfig;
import jmri.jmrix.ieee802154.xbee.XBeeAdapter;
import jmri.jmrix.ieee802154.xbee.XBeeNode;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import java.util.List;
import org.jdom.*;

/**
 * Handle XML persistance of layout connections by persisting
 * the XBeeAdapter (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the XBeeAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2006, 2007, 2008
 * @version $Revision$
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    /**
     * Write out the SerialNode objects too
     * @param e Element being extended
     */
    @Override
    protected void extendElement(Element e) {
        XBeeConnectionMemo xcm; 
        XBeeTrafficController xtc;
        try {
           xcm = (XBeeConnectionMemo)adapter.getSystemConnectionMemo();
           xtc=(XBeeTrafficController)xcm.getTrafficController();
        } catch(NullPointerException npe) {
          // The adapter doesn't have a memo, so no nodes can be defined.
          return;
        }
        try{
           XBeeNode node = (XBeeNode) xtc.getNode(0);
           int index = 1;
           while (node != null) {
               // add node as an element
               Element n = new Element("node");
               n.setAttribute("name",""+node.getNodeAddress());
               e.addContent(n);
               // add parameters to the node as needed
               n.addContent(makeParameter("address", ""+
                      jmri.util.StringUtil.hexStringFromBytes(node.getUserAddress())));
               n.addContent(makeParameter("PAN", ""+
                      jmri.util.StringUtil.hexStringFromBytes(node.getPANAddress())));
               n.addContent(makeParameter("GUID", ""+
                      jmri.util.StringUtil.hexStringFromBytes(node.getGlobalAddress())));
               n.addContent(makeParameter("name", node.getIdentifier()));

               // look for the next node
               node = (XBeeNode) xtc.getNode(index);
               index ++;
           }
        } catch(java.lang.NullPointerException npe2) {
          // no nodes defined.
          return;
        }
    }

	protected Element makeParameter(String name, String value) {
    	Element p = new Element("parameter");
       	p.setAttribute("name",name);
        p.addContent(value);
        return p;
	}

    protected void getInstance() {
        adapter = new XBeeAdapter();
    }

    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig)object).getAdapter();
    }

    /**
     * Unpack the node information when reading the "connection" element
     * @param e Element containing the connection info
     */
    @SuppressWarnings("unchecked")
    protected void unpackElement(Element e) {
        List<Element> l = e.getChildren("node");
        for (int i = 0; i<l.size(); i++) {
            Element n = l.get(i);
            //int addr = Integer.parseInt(n.getAttributeValue("name"));
            byte PAN[] = jmri.util.StringUtil.bytesFromHexString(findParmValue(n,"PAN"));
            byte address[] = jmri.util.StringUtil.bytesFromHexString(findParmValue(n,"address"));
            byte GUID[] = jmri.util.StringUtil.bytesFromHexString(findParmValue(n,"GUID"));
            String Identifier = findParmValue(n,"name");
            // create node (they register themselves)
            XBeeNode node = new XBeeNode(PAN,address,GUID);
            node.setIdentifier(Identifier);

            // Trigger initialization of this Node to reflect these parameters
            XBeeConnectionMemo xcm = (XBeeConnectionMemo)adapter.getSystemConnectionMemo();
            XBeeTrafficController xtc=(XBeeTrafficController)xcm.getTrafficController();
            xtc.registerNode(node);
        }
    }



    /**
     * Service routine to look through "parameter" child elements
     * to find a particular parameter value
     * @param e Element containing parameters
     * @param name name of desired parameter
     * @return String value
     */
    @SuppressWarnings("unchecked")
	String findParmValue(Element e, String name) {
        List<Element> l = e.getChildren("parameter");
        for (int i = 0; i<l.size(); i++) {
            Element n = l.get(i);
            if (n.getAttributeValue("name").equals(name))
                return n.getTextTrim();
        }
        return null;
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }
     
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectionConfigXml.class.getName());

}

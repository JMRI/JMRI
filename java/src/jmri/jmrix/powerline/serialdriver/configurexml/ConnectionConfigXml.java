package jmri.jmrix.powerline.serialdriver.configurexml;

import java.util.List;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.powerline.serialdriver.ConnectionConfig;
import jmri.jmrix.powerline.serialdriver.SerialDriverAdapter;
import org.jdom2.Element;

/**
 * Handle XML persistance of layout connections by persisting the
 * SerialDriverAdapter (and connections). Note this is named as the XML version
 * of a ConnectionConfig object, but it's actually persisting the
 * SerialDriverAdapter.
 * <p>
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

//    /**
//     * Write out the SerialNode objects too
//     * @param e Element being extended
//     */
//    protected void extendElement(Element e) {
//        SerialNode node = SerialTrafficController.instance().getSerialNode(0);
//        int index = 1;
//        while (node != null) {
//            // add node as an element
//            Element n = new Element("node");
//            n.setAttribute("name",""+node.getNodeAddress());
//            e.addContent(n);
//            // add parameters to the node as needed
//            n.addContent(makeParameter("nodetype", ""+node.getNodeType()));
//
//            // look for the next node
//            node = SerialTrafficController.instance().getSerialNode(index);
//            index ++;
//        }
//    }
    protected Element makeParameter(String name, String value) {
        Element p = new Element("parameter");
        p.setAttribute("name", name);
        p.addContent(value);
        return p;
    }

    @Override
    protected void getInstance() {
        adapter = new SerialDriverAdapter();
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

//    /**
//     * Unpack the node information when reading the "connection" element
//     * @param e Element containing the connection info
//     */
// protected void unpackElement(Element e) {
//        List<Element> l = e.getChildren("node");
//        for (int i = 0; i<l.size(); i++) {
//            Element n = l.get(i);
//            int addr = Integer.parseInt(n.getAttributeValue("name"));
//            int type = Integer.parseInt(findParmValue(n,"nodetype"));            
//
//            // create node (they register themselves)
//            SerialNode node = new SerialNode(addr, type);
//            
//            // Trigger initialization of this Node to reflect these parameters
//            SerialTrafficController.instance().initializeSerialNode(node);
//        }
//    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

}

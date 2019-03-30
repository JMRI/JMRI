package jmri.jmrix.secsi.serialdriver.configurexml;

import java.util.List;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;
import jmri.jmrix.secsi.SerialNode;
import jmri.jmrix.secsi.SerialTrafficController;
import jmri.jmrix.secsi.serialdriver.ConnectionConfig;
import jmri.jmrix.secsi.serialdriver.SerialDriverAdapter;
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

    /**
     * Write out the SerialNode objects too
     *
     * @param e Element being extended
     */
    @Override
    protected void extendElement(Element e) {
        SerialNode node = (SerialNode) ((SecsiSystemConnectionMemo)adapter.getSystemConnectionMemo()).getTrafficController().getNode(0);
        int index = 1;
        while (node != null) {
            // add node as an element
            Element n = new Element("node");
            n.setAttribute("name", "" + node.getNodeAddress());
            e.addContent(n);
            // add parameters to the node as needed
            n.addContent(makeParameter("nodetype", "" + node.getNodeType()));

            // look for the next node
            node = (SerialNode) ((SecsiSystemConnectionMemo)adapter.getSystemConnectionMemo()).getTrafficController().getNode(index);
            index++;
        }
    }

    protected Element makeParameter(String name, String value) {
        Element p = new Element("parameter");
        p.setAttribute("name", name);
        p.addContent(value);
        return p;
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new SerialDriverAdapter();
        }
    }

    @Override
    protected void unpackElement(Element shared, Element perNode) {
        List<Element> l = shared.getChildren("node");
        for (int i = 0; i < l.size(); i++) {
            Element n = l.get(i);
            int addr = Integer.parseInt(n.getAttributeValue("name"));
            int type = Integer.parseInt(findParmValue(n, "nodetype"));
 
            SerialTrafficController tc = ((SecsiSystemConnectionMemo)adapter.getSystemConnectionMemo()).getTrafficController();
            // create node (they register themselves)
            SerialNode node = new SerialNode(addr, type, tc);
            // Trigger initialization of this Node to reflect these parameters
            tc.initializeSerialNode(node);
        }
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

}

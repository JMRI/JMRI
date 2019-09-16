package jmri.jmrix.ieee802154.serialdriver.configurexml;

import java.util.List;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.ieee802154.serialdriver.ConnectionConfig;
import jmri.jmrix.ieee802154.serialdriver.SerialDriverAdapter;
import jmri.jmrix.ieee802154.serialdriver.SerialNode;
import jmri.jmrix.ieee802154.serialdriver.SerialSystemConnectionMemo;
import jmri.jmrix.ieee802154.serialdriver.SerialTrafficController;
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
        SerialSystemConnectionMemo scm;
        try {
            scm = (SerialSystemConnectionMemo) adapter.getSystemConnectionMemo();
        } catch (NullPointerException npe) {
            // The adapter doesn't have a memo, so no nodes can be defined.
            return;
        }
        SerialTrafficController stc = (SerialTrafficController) scm.getTrafficController();
        SerialNode node = (SerialNode) stc.getNode(0);
        int index = 1;
        while (node != null) {
            // add node as an element
            Element n = new Element("node");
            n.setAttribute("name", "" + node.getNodeAddress());
            e.addContent(n);
            // add parameters to the node as needed
            n.addContent(makeParameter("PAN", ""
                    + jmri.util.StringUtil.hexStringFromBytes(node.getPANAddress())));
            n.addContent(makeParameter("address", ""
                    + jmri.util.StringUtil.hexStringFromBytes(node.getUserAddress())));
            n.addContent(makeParameter("GUID", ""
                    + jmri.util.StringUtil.hexStringFromBytes(node.getGlobalAddress())));

            // look for the next node
            node = (SerialNode) stc.getNode(index);
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
    protected void getInstance() {
        adapter = new SerialDriverAdapter();
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void unpackElement(Element shared, Element perNode) {
        List<Element> l = shared.getChildren("node");
        for (int i = 0; i < l.size(); i++) {
            Element n = l.get(i);
            //int addr = Integer.parseInt(n.getAttributeValue("name"));
            byte PAN[] = jmri.util.StringUtil.bytesFromHexString(findParmValue(n, "PAN"));
            byte address[] = jmri.util.StringUtil.bytesFromHexString(findParmValue(n, "address"));
            byte GUID[] = jmri.util.StringUtil.bytesFromHexString(findParmValue(n, "GUID"));

            // create node (they register themselves)
            SerialNode node = new SerialNode(PAN, address, GUID);

            // Trigger initialization of this Node to reflect these parameters
            SerialSystemConnectionMemo scm = (SerialSystemConnectionMemo) adapter.getSystemConnectionMemo();
            SerialTrafficController stc = (SerialTrafficController) scm.getTrafficController();
            stc.registerNode(node);
        }
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

}

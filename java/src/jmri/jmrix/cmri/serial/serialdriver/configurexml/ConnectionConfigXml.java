package jmri.jmrix.cmri.serial.serialdriver.configurexml;

import java.util.List;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialNode;
import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.jmrix.cmri.serial.serialdriver.ConnectionConfig;
import jmri.jmrix.cmri.serial.serialdriver.SerialDriverAdapter;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import org.jdom2.Element;

/**
 * Handle XML persistance of layout connections by persisting the
 * SerialDriverAdapter (and connections). Note this is named as the XML version
 * of a ConnectionConfig object, but it's actually persisting the
 * SerialDriverAdapter.
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

    /**
     * Write out the SerialNode objects too
     *
     * @param e Element being extended
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION")
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    protected void extendElement(Element e) {
        SerialNode node = (SerialNode) SerialTrafficController.instance().getNode(0);
        int index = 1;
        while (node != null) {
            // add node as an element
            Element n = new Element("node");
            n.setAttribute("name", "" + node.getNodeAddress());
            e.addContent(n);
            // add parameters to the node as needed
            n.addContent(makeParameter("nodetype", "" + node.getNodeType()));
            n.addContent(makeParameter("bitspercard", "" + node.getNumBitsPerCard()));
            n.addContent(makeParameter("transmissiondelay", "" + node.getTransmissionDelay()));
            n.addContent(makeParameter("num2lsearchlights", "" + node.getNum2LSearchLights()));
            n.addContent(makeParameter("pulsewidth", "" + node.getPulseWidth()));
            String value = "";
            for (int i = 0; i < node.getLocSearchLightBits().length; i++) {
                value = value + Integer.toHexString(node.getLocSearchLightBits()[i] & 0xF);
            }
            n.addContent(makeParameter("locsearchlightbits", "" + value));
            value = "";
            for (int i = 0; i < node.getCardTypeLocation().length; i++) {
                value = value + Integer.toHexString(node.getCardTypeLocation()[i] & 0xF);
            }
            n.addContent(makeParameter("cardtypelocation", "" + value));

            // look for the next node
            node = (SerialNode) SerialTrafficController.instance().getNode(index);
            index++;
        }
    }

    protected Element makeParameter(String name, String value) {
        Element p = new Element("parameter");
        p.setAttribute("name", name);
        p.addContent(value);
        return p;
    }

    protected void getInstance() {
        adapter = SerialDriverAdapter.instance();
    }

    @Override
    protected void unpackElement(Element shared, Element perNode) {
        List<Element> l = shared.getChildren("node");
        for (int i = 0; i < l.size(); i++) {
            Element n = l.get(i);
            int addr = Integer.parseInt(n.getAttributeValue("name"));
            int type = Integer.parseInt(findParmValue(n, "nodetype"));
            int bpc = Integer.parseInt(findParmValue(n, "bitspercard"));
            int delay = Integer.parseInt(findParmValue(n, "transmissiondelay"));
            int num2l = Integer.parseInt(findParmValue(n, "num2lsearchlights"));
            int pulseWidth = 500;
            if ((findParmValue(n, "pulsewidth")) != null) {
                pulseWidth = Integer.parseInt(findParmValue(n, "pulsewidth"));
            }

            String slb = findParmValue(n, "locsearchlightbits");
            String ctl = findParmValue(n, "cardtypelocation");

            // create node (they register themselves)
            SerialNode node = new SerialNode(addr, type,((CMRISystemConnectionMemo)adapter.getSystemConnectionMemo()).getTrafficController());
            node.setNumBitsPerCard(bpc);
            node.setTransmissionDelay(delay);
            node.setNum2LSearchLights(num2l);
            node.setPulseWidth(pulseWidth);

            for (int j = 0; j < slb.length(); j++) {
                node.setLocSearchLightBits(j, (slb.charAt(j) - '0'));
            }

            for (int j = 0; j < ctl.length(); j++) {
                node.setCardTypeLocation(j, (ctl.charAt(j) - '0'));
            }

            // Trigger initialization of this Node to reflect these parameters
            SerialTrafficController.instance().initializeSerialNode(node);
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

}

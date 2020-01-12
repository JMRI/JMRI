package jmri.jmrix.cmri.serial.networkdriver.configurexml;

import java.util.List;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialNode;
import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.jmrix.cmri.serial.networkdriver.ConnectionConfig;
import jmri.jmrix.cmri.serial.networkdriver.NetworkDriverAdapter;
import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import org.jdom2.Element;

/**
 * Handle XML persistence of layout connections by persisting the
 * NetworkDriverAdapter (and connections).
 * <p>
 * Note this is named as the XML version of a ConnectionConfig object, but it's
 * actually persisting the NetworkDriverAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2015
 * @author kcameron Copyright (C) 2010 added multiple connections
 */
public class ConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if(adapter == null) {
           adapter = new NetworkDriverAdapter();
           adapter.configure(); // sets the memo and traffic controller.
        }
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    /**
     * Write out the SerialNode objects too
     *
     * @param e Element being extended
     */
    @Override
    protected void extendElement(Element e) {
        SerialTrafficController tc = ((CMRISystemConnectionMemo)adapter.getSystemConnectionMemo()).getTrafficController();
        SerialNode node = (SerialNode) tc.getNode(0);
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
            StringBuilder value = new StringBuilder("");
            for (int i = 0; i < node.getLocSearchLightBits().length; i++) {
                value.append(Integer.toHexString(node.getLocSearchLightBits()[i] & 0xF));
            }
            n.addContent(makeParameter("locsearchlightbits", value.toString()));
            value = new StringBuilder("");
            for (int i = 0; i < node.getCardTypeLocation().length; i++) {
                value.append(Integer.toHexString(node.getCardTypeLocation()[i] & 0xF));
            }
            n.addContent(makeParameter("cardtypelocation", value.toString()));

            // look for the next node
            node = (SerialNode) tc.getNode(index);
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
            ((CMRISystemConnectionMemo)adapter.getSystemConnectionMemo()).getTrafficController().initializeSerialNode(node);
        }
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

}

package jmri.jmrix.maple.serialdriver.configurexml;

import java.util.List;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.maple.InputBits;
import jmri.jmrix.maple.OutputBits;
import jmri.jmrix.maple.SerialNode;
import jmri.jmrix.maple.MapleSystemConnectionMemo;
import jmri.jmrix.maple.serialdriver.ConnectionConfig;
import jmri.jmrix.maple.serialdriver.SerialDriverAdapter;
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
 * NOTE: Code related to pulsed turnout control has been commented out.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2008
 * @author Bob Jacobsen, Dave Duchamp Copyright (c) 2009 - Maple modifications
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
        SerialNode node = (SerialNode) ((MapleSystemConnectionMemo)adapter.getSystemConnectionMemo()).getTrafficController().getNode(0);
        int index = 1;
        while (node != null) {
            // add node as an element
            Element n = new Element("node");
            n.setAttribute("name", "" + node.getNodeAddress());
            e.addContent(n);
            // add parameters to the node as needed
            n.addContent(makeParameter("transmissiondelay", "" + InputBits.getTimeoutTime()));
            n.addContent(makeParameter("inputbits", "" + InputBits.getNumInputBits()));
            n.addContent(makeParameter("senddelay", "" + OutputBits.getSendDelay()));
            n.addContent(makeParameter("outputbits", "" + OutputBits.getNumOutputBits()));
//            n.addContent(makeParameter("pulsewidth", ""+node.getPulseWidth()));

            // look for the next node
            node = (SerialNode) ((MapleSystemConnectionMemo)adapter.getSystemConnectionMemo()).getTrafficController().getNode(index);
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
        if(adapter == null ) {
           adapter = new SerialDriverAdapter();
        }
    }

    @Override
    protected void unpackElement(Element shared, Element perNode) {
        List<Element> l = shared.getChildren("node");
        for (int i = 0; i < l.size(); i++) {
            Element n = l.get(i);
            int addr = Integer.parseInt(n.getAttributeValue("name"));
            int delay = Integer.parseInt(findParmValue(n, "transmissiondelay"));
            int senddelay = Integer.parseInt(findParmValue(n, "senddelay"));
            int numinput = Integer.parseInt(findParmValue(n, "inputbits"));
            int numoutput = Integer.parseInt(findParmValue(n, "outputbits"));
//   int pulseWidth = 500;
//   if ((findParmValue(n,"pulsewidth")) != null) {
//    pulseWidth = Integer.parseInt(findParmValue(n,"pulsewidth"));
//   }

            // create node (they register themselves)
            SerialNode node = new SerialNode(addr, 0, ((MapleSystemConnectionMemo)adapter.getSystemConnectionMemo()).getTrafficController());
            InputBits.setTimeoutTime(delay);
            InputBits.setNumInputBits(numinput);
            OutputBits.setSendDelay(senddelay);
            OutputBits.setNumOutputBits(numoutput);
//   node.setPulseWidth(pulseWidth);

            // Trigger initialization of this Node to reflect these parameters
            ((MapleSystemConnectionMemo)adapter.getSystemConnectionMemo()).getTrafficController().initializeSerialNode(node);
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

    @Override
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

}

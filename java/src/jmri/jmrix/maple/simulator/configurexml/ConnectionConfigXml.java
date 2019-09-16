package jmri.jmrix.maple.simulator.configurexml;

import java.util.List;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.maple.InputBits;
import jmri.jmrix.maple.MapleSystemConnectionMemo;
import jmri.jmrix.maple.OutputBits;
import jmri.jmrix.maple.SerialNode;
import jmri.jmrix.maple.SerialTrafficController;
import jmri.jmrix.maple.simulator.ConnectionConfig;
import jmri.jmrix.maple.simulator.SimulatorAdapter;
import org.jdom2.Element;

/**
 * Handle XML persistence of layout connections by persisting the
 * SerialDriverAdapter (and connections).
 * <p>
 * Note this is named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the SerialDriverAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright (c) 2003 copied from NCE/Tams code
 * @author kcameron Copyright (c) 2014
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    /**
     * Write out the SerialNode objects too.
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
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new SimulatorAdapter();
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

            SerialTrafficController tc = ((MapleSystemConnectionMemo) adapter.getSystemConnectionMemo()).getTrafficController();
            // create node (they register themselves)
            SerialNode node = new SerialNode(addr, 0, tc);
            InputBits.setTimeoutTime(delay);
            InputBits.setNumInputBits(numinput);
            OutputBits.setSendDelay(senddelay);
            OutputBits.setNumOutputBits(numoutput);

            // Trigger initialization of this Node to reflect these parameters
            tc.initializeSerialNode(node);
        }
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

}

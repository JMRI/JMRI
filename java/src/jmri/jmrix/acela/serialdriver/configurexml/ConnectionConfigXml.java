package jmri.jmrix.acela.serialdriver.configurexml;

import java.util.List;
import jmri.jmrix.acela.AcelaNode;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import jmri.jmrix.acela.AcelaTrafficController;
import jmri.jmrix.acela.serialdriver.ConnectionConfig;
import jmri.jmrix.acela.serialdriver.SerialDriverAdapter;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of layout connections by persistening the
 * SerialDriverAdapter (and connections). Note this is named as the XML version
 * of a ConnectionConfig object, but it's actually persisting the
 * SerialDriverAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author Bob Coleman, Copyright (c) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
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
        AcelaTrafficController tc = ((AcelaSystemConnectionMemo) adapter.getSystemConnectionMemo()).getTrafficController();
        AcelaNode node = (AcelaNode) tc.getNode(0);
        int index = 1;
        while (node != null) {
            // add node as an element
            Element n = new Element("node");
            n.setAttribute("name", "" + node.getNodeAddress());
            e.addContent(n);
            // add parameters to the node as needed
            n.addContent(makeParameter("nodetype", "" + node.getNodeTypeString()));
            if (node.getNodeType() == AcelaNode.TB) {
                for (int s = 0; s < 4; s++) {
                    n.addContent(makeParameter("sensortype" + s, "" + node.getSensorTypeString(s)));
                    n.addContent(makeParameter("sensorpolarity" + s, "" + node.getSensorPolarityString(s)));
                    n.addContent(makeParameter("sensorthreshold" + s, "" + node.getSensorThreshold(s)));
                }
                for (int o = 0; o < 4; o++) {
                    n.addContent(makeParameter("outputwired" + o, "" + node.getOutputWiredString(o)));
                    n.addContent(makeParameter("outputinit" + o, "" + node.getOutputInitString(o)));
                    n.addContent(makeParameter("outputtype" + o, "" + node.getOutputTypeString(o)));
                    n.addContent(makeParameter("outputlength" + o, "" + node.getOutputLength(o)));
                }
            } else if (node.getNodeType() == AcelaNode.D8) {
                for (int o = 0; o < 8; o++) {
                    n.addContent(makeParameter("outputwired" + o, "" + node.getOutputWiredString(o)));
                    n.addContent(makeParameter("outputinit" + o, "" + node.getOutputInitString(o)));
                    n.addContent(makeParameter("outputtype" + o, "" + node.getOutputTypeString(o)));
                    n.addContent(makeParameter("outputlength" + o, "" + node.getOutputLength(o)));
                }
            } else if (node.getNodeType() == AcelaNode.WM) {
                for (int s = 0; s < 8; s++) {
                    n.addContent(makeParameter("sensortype" + s, "" + node.getSensorTypeString(s)));
                    n.addContent(makeParameter("sensorpolarity" + s, "" + node.getSensorPolarityString(s)));
                    n.addContent(makeParameter("sensorthreshold" + s, "" + node.getSensorThreshold(s)));
                }
            } else if (node.getNodeType() == AcelaNode.SM) {
                for (int o = 0; o < 16; o++) {
                    n.addContent(makeParameter("outputwired" + o, "" + node.getOutputWiredString(o)));
                    n.addContent(makeParameter("outputinit" + o, "" + node.getOutputInitString(o)));
                    n.addContent(makeParameter("outputtype" + o, "" + node.getOutputTypeString(o)));
                    n.addContent(makeParameter("outputlength" + o, "" + node.getOutputLength(o)));
                }
            } else if (node.getNodeType() == AcelaNode.SW) {
                for (int o = 0; o < 16; o++) {
                    n.addContent(makeParameter("outputwired" + o, "" + node.getOutputWiredString(o)));
                    n.addContent(makeParameter("outputinit" + o, "" + node.getOutputInitString(o)));
                    n.addContent(makeParameter("outputtype" + o, "" + node.getOutputTypeString(o)));
                    n.addContent(makeParameter("outputlength" + o, "" + node.getOutputLength(o)));
                }
            } else if (node.getNodeType() == AcelaNode.YM) {
                for (int o = 0; o < 16; o++) {
                    n.addContent(makeParameter("outputwired" + o, "" + node.getOutputWiredString(o)));
                    n.addContent(makeParameter("outputinit" + o, "" + node.getOutputInitString(o)));
                    n.addContent(makeParameter("outputtype" + o, "" + node.getOutputTypeString(o)));
                    n.addContent(makeParameter("outputlength" + o, "" + node.getOutputLength(o)));
                }
            } else if (node.getNodeType() == AcelaNode.SY) {
                for (int s = 0; s < 16; s++) {
                    n.addContent(makeParameter("sensortype" + s, "" + node.getSensorTypeString(s)));
                    n.addContent(makeParameter("sensorpolarity" + s, "" + node.getSensorPolarityString(s)));
                    n.addContent(makeParameter("sensorthreshold" + s, "" + node.getSensorThreshold(s)));
                }
            }
            // look for the next node
            node = (AcelaNode) tc.getNode(index);
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
            String nodetypestring = findParmValue(n, "nodetype");
            int type = AcelaNode.moduleTypes.lastIndexOf(nodetypestring) / 2;

            // create node (they register themselves)
            AcelaNode node = new AcelaNode(addr, type, ((jmri.jmrix.acela.serialdriver.SerialDriverAdapter) adapter).getSystemConnectionMemo().getTrafficController());
            log.info("Created a new Acela Node [{}] as a result of a configuration file of type: {}", addr, type);

            if (type == AcelaNode.TB) {
                for (int s = 0; s < 4; s++) {
                    String sensortype = findParmValue(n, "sensortype" + s);
                    String sensorpolarity = findParmValue(n, "sensorpolarity" + s);
                    int sensorthreshold = Integer.parseInt(findParmValue(n, "sensorthreshold" + s));
                    node.setSensorTypeString(s, sensortype);
                    node.setSensorPolarityString(s, sensorpolarity);

                    if (sensorthreshold < 0) {
                        sensorthreshold = 0;
                    }
                    if (sensorthreshold > 31) {
                        sensorthreshold = 31;
                    }
                    node.setSensorThreshold(s, sensorthreshold);

                }
                for (int o = 0; o < 4; o++) {
                    String outputwired = findParmValue(n, "outputwired" + o);
                    String outputinit = findParmValue(n, "outputinit" + o);
                    String outputtype = findParmValue(n, "outputtype" + o);
                    String outputlengths = findParmValue(n, "outputlength" + o);
                    // This can be removed in June 2010:
                    if (outputlengths == null) {
                        outputlengths = AcelaNode.outputLEN0;
                    }
                    int outputlength = Integer.parseInt(outputlengths);
                    node.setOutputWiredString(o, outputwired);
                    node.setOutputInitString(o, outputinit);
                    // This can be removed in June 2010:
                    if (outputtype == null) {
                        outputtype = AcelaNode.outputONOFF;
                    }
                    node.setOutputTypeString(o, outputtype);
                    if (outputlength < 0) {
                        outputlength = 0;
                    }
                    if (outputlength > 255) {
                        outputlength = 255;
                    }
                    node.setOutputLength(o, outputlength);
                }
            } else if (type == AcelaNode.D8) {
                for (int o = 0; o < 8; o++) {
                    String outputwired = findParmValue(n, "outputwired" + o);
                    String outputinit = findParmValue(n, "outputinit" + o);
                    String outputtype = findParmValue(n, "outputtype" + o);
                    String outputlengths = findParmValue(n, "outputlength" + o);
                    // This can be removed in June 2010:
                    if (outputlengths == null) {
                        outputlengths = AcelaNode.outputLEN0;
                    }
                    int outputlength = Integer.parseInt(outputlengths);
                    node.setOutputWiredString(o, outputwired);
                    node.setOutputInitString(o, outputinit);
                    // This can be removed in June 2010:
                    if (outputtype == null) {
                        outputtype = AcelaNode.outputONOFF;
                    }
                    node.setOutputTypeString(o, outputtype);
                    if (outputlength < 0) {
                        outputlength = 0;
                    }
                    if (outputlength > 255) {
                        outputlength = 255;
                    }
                    node.setOutputLength(o, outputlength);
                }
            } else if (type == AcelaNode.WM) {
                for (int s = 0; s < 8; s++) {
                    String sensortype = findParmValue(n, "sensortype" + s);
                    String sensorpolarity = findParmValue(n, "sensorpolarity" + s);
                    int sensorthreshold = Integer.parseInt(findParmValue(n, "sensorthreshold" + s));
                    node.setSensorTypeString(s, sensortype);
                    node.setSensorPolarityString(s, sensorpolarity);

                    if (sensorthreshold < 0) {
                        sensorthreshold = 0;
                    }
                    if (sensorthreshold > 31) {
                        sensorthreshold = 31;
                    }
                    node.setSensorThreshold(s, sensorthreshold);

                }
            } else if (type == AcelaNode.SM) {
                for (int o = 0; o < 16; o++) {
                    String outputwired = findParmValue(n, "outputwired" + o);
                    // This can be removed in June 2010:
                    if (outputwired == null) {
                        outputwired = AcelaNode.outputNO;
                    }
                    String outputinit = findParmValue(n, "outputinit" + o);
                    String outputtype = findParmValue(n, "outputtype" + o);
                    String outputlengths = findParmValue(n, "outputlength" + o);
                    // This can be removed in June 2010:
                    if (outputlengths == null) {
                        outputlengths = AcelaNode.outputLEN0;
                    }
                    int outputlength = Integer.parseInt(outputlengths);
                    node.setOutputInitString(o, outputinit);
                    // This can be removed in June 2010:
                    if (outputtype == null) {
                        outputtype = AcelaNode.outputONOFF;
                    }
                    node.setOutputTypeString(o, outputtype);
                    if (outputlength < 0) {
                        outputlength = 0;
                    }
                    if (outputlength > 255) {
                        outputlength = 255;
                    }
                    node.setOutputLength(o, outputlength);
                }
            } else if (type == AcelaNode.SW) {
                for (int o = 0; o < 16; o++) {
                    String outputwired = findParmValue(n, "outputwired" + o);
                    // This can be removed in June 2010:
                    if (outputwired == null) {
                        outputwired = AcelaNode.outputNO;
                    }
                    String outputinit = findParmValue(n, "outputinit" + o);
                    String outputtype = findParmValue(n, "outputtype" + o);
                    String outputlengths = findParmValue(n, "outputlength" + o);
                    // This can be removed in June 2010:
                    if (outputlengths == null) {
                        outputlengths = AcelaNode.outputLEN0;
                    }
                    int outputlength = Integer.parseInt(outputlengths);
                    node.setOutputInitString(o, outputinit);
                    // This can be removed in June 2010:
                    if (outputtype == null) {
                        outputtype = AcelaNode.outputONOFF;
                    }
                    node.setOutputTypeString(o, outputtype);
                    if (outputlength < 0) {
                        outputlength = 0;
                    }
                    if (outputlength > 255) {
                        outputlength = 255;
                    }
                    node.setOutputLength(o, outputlength);
                }
            } else if (type == AcelaNode.YM) {
                for (int o = 0; o < 16; o++) {
                    String outputwired = findParmValue(n, "outputwired" + o);
                    // This can be removed in June 2010:
                    if (outputwired == null) {
                        outputwired = AcelaNode.outputNO;
                    }
                    String outputinit = findParmValue(n, "outputinit" + o);
                    String outputtype = findParmValue(n, "outputtype" + o);
                    String outputlengths = findParmValue(n, "outputlength" + o);
                    // This can be removed in June 2010:
                    if (outputlengths == null) {
                        outputlengths = AcelaNode.outputLEN0;
                    }
                    int outputlength = Integer.parseInt(outputlengths);
                    node.setOutputInitString(o, outputinit);
                    // This can be removed in June 2010:
                    if (outputtype == null) {
                        outputtype = AcelaNode.outputONOFF;
                    }
                    node.setOutputTypeString(o, outputtype);
                    if (outputlength < 0) {
                        outputlength = 0;
                    }
                    if (outputlength > 255) {
                        outputlength = 255;
                    }
                    node.setOutputLength(o, outputlength);
                }
            } else if (type == AcelaNode.SY) {
                for (int s = 0; s < 16; s++) {
                    String sensortype = findParmValue(n, "sensortype" + s);
                    String sensorpolarity = findParmValue(n, "sensorpolarity" + s);
                    int sensorthreshold = Integer.parseInt(findParmValue(n, "sensorthreshold" + s));
                    node.setSensorTypeString(s, sensortype);
                    node.setSensorPolarityString(s, sensorpolarity);

                    if (sensorthreshold < 0) {
                        sensorthreshold = 0;
                    }
                    if (sensorthreshold > 31) {
                        sensorthreshold = 31;
                    }
                    node.setSensorThreshold(s, sensorthreshold);

                }
            }

            AcelaTrafficController tc = ((AcelaSystemConnectionMemo) adapter.getSystemConnectionMemo()).getTrafficController();
            // Do not poll for Acela network nodes
            tc.setNeedToPollNodes(false);

            // Trigger initialization of this Node to reflect these parameters
            tc.initializeAcelaNode(node);
        }
        // Do not let the Acela network poll until we are really ready
        ((AcelaSystemConnectionMemo) adapter.getSystemConnectionMemo()).getTrafficController().setReallyReadyToPoll(true);
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new SerialDriverAdapter();
        }
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class);

}

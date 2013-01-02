package jmri.jmrix.rps.serial.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.rps.serial.ConnectionConfig;
import jmri.jmrix.rps.serial.SerialAdapter;
import java.util.List;
import org.jdom.*;

/**
 * Handle XML persistance of layout connections by persisting
 * the SeriaAdapter (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the SerialAdapter.
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
    protected void extendElement(Element e) {
/*         SerialNode node = (SerialNode) SerialTrafficController.instance().getNode(0); */
/*         int index = 1; */
/*         while (node != null) { */
/*             // add node as an element */
/*             Element n = new Element("node"); */
/*             n.setAttribute("name",""+node.getNodeAddress()); */
/*             e.addContent(n); */
/*             // add parameters to the node as needed */
/*             n.addContent(makeParameter("nodetype", ""+node.getNodeType())); */
/*  */
/*             // look for the next node */
/*             node = (SerialNode) SerialTrafficController.instance().getNode(index); */
/*             index ++; */
/*         } */
    }

	protected Element makeParameter(String name, String value) {
    	Element p = new Element("parameter");
       	p.setAttribute("name",name);
        p.addContent(value);
        return p;
	}

    protected void getInstance() {
        adapter = SerialAdapter.instance();
    }

    /**
     * Unpack the node information when reading the "connection" element
     * @param e Element containing the connection info
     */
    protected void unpackElement(Element e) {
/*         List l = e.getChildren("node"); */
/*         for (int i = 0; i<l.size(); i++) { */
/*             Element n = (Element) l.get(i); */
/*             int addr = Integer.parseInt(n.getAttributeValue("name")); */
/*             int type = Integer.parseInt(findParmValue(n,"nodetype"));             */
/*  */
/*             // create node (they register themselves) */
/*             SerialNode node = new SerialNode(addr, type); */
/*              */
/*             // Trigger initialization of this Node to reflect these parameters */
/*             SerialTrafficController.instance().initializeSerialNode(node); */
/*         } */
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
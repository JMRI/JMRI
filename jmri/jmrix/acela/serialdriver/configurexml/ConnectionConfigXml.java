// ConnectionConfigXml.java

package jmri.jmrix.acela.serialdriver.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.acela.serialdriver.ConnectionConfig;
import jmri.jmrix.acela.serialdriver.SerialDriverAdapter;
import jmri.jmrix.acela.AcelaNode;
import jmri.jmrix.acela.AcelaTrafficController;

import java.util.List;
import org.jdom.*;

/**
 * Handle XML persistance of layout connections by persistening
 * the SerialDriverAdapter (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the SerialDriverAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author      Bob Jacobsen Copyright: Copyright (c) 2003
 * @version     $Revision: 1.3 $
 *
 * @author      Bob Coleman, Copyright (c) 2007, 2008
 *              Based on CMRI serial example, modified to establish Acela support. 
 */
public class ConnectionConfigXml extends AbstractConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    /**
     * Write out the SerialNode objects too
     * @param e Element being extended
     */
    protected void extendElement(Element e) {
        AcelaNode node = AcelaTrafficController.instance().getAcelaNode(0);
        int index = 1;
        while (node != null) {
            // add node as an element
            Element n = new Element("node");
            n.setAttribute("name",""+node.getNodeAddress());
            e.addContent(n);
            // add parameters to the node as needed
            n.addContent(makeParameter("nodetype", ""+node.getNodeType()));

            // look for the next node
            node = AcelaTrafficController.instance().getAcelaNode(index);
            index ++;
        }
    }

	protected Element makeParameter(String name, String value) {
    	Element p = new Element("parameter");
       	p.setAttribute("name",name);
        p.addContent(value);
        return p;
	}

    protected void getInstance() {
        adapter = SerialDriverAdapter.instance();
    }

    /**
     * Unpack the node information when reading the "connection" element
     * @param e Element containing the connection info
     */
    protected void unpackElement(Element e) {
        List l = e.getChildren("node");
        for (int i = 0; i<l.size(); i++) {
            Element n = (Element) l.get(i);
            int addr = Integer.parseInt(n.getAttributeValue("name"));
            int type = Integer.parseInt(findParmValue(n,"nodetype"));            

            // create node (they register themselves)
            AcelaNode node = new AcelaNode(addr, type);
            
            // Do not poll for Acela network nodes
            AcelaTrafficController.instance().setNeedToPollNodes(false);
            
            // Trigger initialization of this Node to reflect these parameters
            AcelaTrafficController.instance().initializeAcelaNode(node);
        }
    }

    /**
     * Service routine to look through "parameter" child elements
     * to find a particular parameter value
     * @param node Element containing parameters
     * @param name name of desired parameter
     * @return String value
     */
    String findParmValue(Element e, String name) {
        List l = e.getChildren("parameter");
        for (int i = 0; i<l.size(); i++) {
            Element n = (Element) l.get(i);
            if (n.getAttributeValue("name").equals(name))
                return n.getTextTrim();
        }
        return null;
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConnectionConfigXml.class.getName());
}

/* @(#)ConnectionConfigXml.java */

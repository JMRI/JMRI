package jmri.jmrix.cmri.serial.serialdriver.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.cmri.serial.serialdriver.ConnectionConfig;
import jmri.jmrix.cmri.serial.serialdriver.SerialDriverAdapter;
import jmri.jmrix.cmri.serial.*;
import com.sun.java.util.collections.List;
import org.jdom.*;

/**
 * Handle XML persistance of layout connections by persisting
 * the SerialDriverAdapter (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the SerialDriverAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.6 $
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
        SerialNode node = SerialTrafficController.instance().getSerialNode(0);
        int index = 1;
        while (node != null) {
            // add node as an element
            Element n = new Element("node");
            n.addAttribute("name",""+node.getNodeAddress());
            e.addContent(n);
            // add parameters to the node as needed
            n.addContent(makeParameter("nodetype", ""+node.getNodeType()));
            n.addContent(makeParameter("bitspercard", ""+node.getNumBitsPerCard()));
            n.addContent(makeParameter("transmissiondelay", ""+node.getTransmissionDelay()));
            n.addContent(makeParameter("num2lsearchlights", ""+node.getNum2LSearchLights()));
            String value = "";
            for (int i=0; i<node.getLocSearchLightBits().length; i++) {
            	value = value + Integer.toHexString(node.getLocSearchLightBits()[i]&0xF);
            }
            n.addContent(makeParameter("locsearchlightbits", ""+value));
            value = "";
            for (int i=0; i<node.getCardTypeLocation().length; i++) {
            	value = value + Integer.toHexString(node.getCardTypeLocation()[i]&0xF);
            }
            n.addContent(makeParameter("cardtypelocation", ""+value));

            // look for the next node
            node = SerialTrafficController.instance().getSerialNode(index);
            index ++;
        }
    }

	protected Element makeParameter(String name, String value) {
    	Element p = new Element("parameter");
       	p.addAttribute("name",name);
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
            int bpc = Integer.parseInt(findParmValue(n,"bitspercard"));
            int delay = Integer.parseInt(findParmValue(n,"transmissiondelay"));
            int num2l = Integer.parseInt(findParmValue(n,"num2lsearchlights"));
            
            String slb = findParmValue(n,"locsearchlightbits");
            String ctl = findParmValue(n,"cardtypelocation");
            

            // create node (they register themselves)
            SerialNode node = new SerialNode(addr, type);
            node.setNumBitsPerCard(bpc);
            node.setTransmissionDelay(delay);
            node.setNum2LSearchLights(num2l);
            
            for (int j = 0; j<slb.length(); j++) {
            	node.setLocSearchLightBits(j, (slb.charAt(j)-'0') );
            }
            
            for  (int j = 0; j<ctl.length(); j++) {
            	node.setCardTypeLocation(j, (ctl.charAt(j)-'0') );
            }
            

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
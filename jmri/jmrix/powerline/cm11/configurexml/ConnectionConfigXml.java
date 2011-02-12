package jmri.jmrix.powerline.cm11.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.powerline.cm11.ConnectionConfig;
import jmri.jmrix.powerline.cm11.SpecificDriverAdapter;

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
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2006, 2007, 2008
 * @version $Revision: 1.1 $
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

//	protected Element makeParameter(String name, String value) {
//    	Element p = new Element("parameter");
//       	p.setAttribute("name",name);
//        p.addContent(value);
//        return p;
//	}

    protected void getInstance() {
        adapter = new SpecificDriverAdapter();
    }

    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig)object).getAdapter();
    }

    /**
     * Service routine to look through "parameter" child elements
     * to find a particular parameter value
     * @param node Element containing parameters
     * @param name name of desired parameter
     * @return String value
     */
//    @SuppressWarnings("unchecked")
//	String findParmValue(Element e, String name) {
//        List<Element> l = e.getChildren("parameter");
//        for (int i = 0; i<l.size(); i++) {
//            Element n = l.get(i);
//            if (n.getAttributeValue("name").equals(name))
//                return n.getTextTrim();
//        }
//        return null;
//    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }
     
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectionConfigXml.class.getName());

}
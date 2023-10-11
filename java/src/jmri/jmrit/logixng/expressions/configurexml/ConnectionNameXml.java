package jmri.jmrit.logixng.expressions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.expressions.ConnectionName;

import org.jdom2.Element;

/**
 * Handle XML configuration for ConnectionName objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class ConnectionNameXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ConnectionNameXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type ConnectionName
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ConnectionName p = (ConnectionName) o;

        Element element = new Element("ConnectionName");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(new Element("manufacturer").addContent(p.getManufacturer()));
        element.addContent(new Element("connectionName").addContent(p.getConnectionName()));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ConnectionName h = new ConnectionName(sys, uname);

        loadCommon(h, shared);

        if (shared.getChild("manufacturer") != null) {
            h.setManufacturer(shared.getChild("manufacturer").getTextTrim());
        }
        if (shared.getChild("connectionName") != null) {
            h.setConnectionName(shared.getChild("connectionName").getTextTrim());
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnectionNameXml.class);
}

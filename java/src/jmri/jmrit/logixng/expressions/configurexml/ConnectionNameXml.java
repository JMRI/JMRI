package jmri.jmrit.logixng.expressions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.expressions.ConnectionName;

import org.jdom2.Element;

import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectStringXml;

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

        var selectConnectionNameXml = new LogixNG_SelectStringXml();

        Element element = new Element("ConnectionName");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(selectConnectionNameXml.store(p.getSelectConnectionName(), "connectionName"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ConnectionName h = new ConnectionName(sys, uname);

        loadCommon(h, shared);

        var selectConnectionNameXml = new LogixNG_SelectStringXml();

        selectConnectionNameXml.load(shared.getChild("connectionName"), h.getSelectConnectionName());

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnectionNameXml.class);
}

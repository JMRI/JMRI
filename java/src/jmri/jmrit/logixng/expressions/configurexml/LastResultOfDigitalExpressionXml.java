package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.LastResultOfDigitalExpression;

import org.jdom2.Element;

/**
 * Handle XML configuration for LastResultOfDigitalExpression objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class LastResultOfDigitalExpressionXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public LastResultOfDigitalExpressionXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        LastResultOfDigitalExpression p = (LastResultOfDigitalExpression) o;

        Element element = new Element("LastResultOfDigitalExpression");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var handle = p.getDigitalExpression();
        if (handle != null) {
            element.addContent(new Element("expression").addContent(handle.getName()));
        }

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        LastResultOfDigitalExpression h = new LastResultOfDigitalExpression(sys, uname);

        loadCommon(h, shared);

        Element lightName = shared.getChild("expression");
        if (lightName != null) {
            DigitalExpressionBean t = InstanceManager
                    .getDefault(DigitalExpressionManager.class)
                    .getNamedBean(lightName.getTextTrim());
            if (t != null) h.setDigitalExpression(t);
            else h.removeDigitalExpression();
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LastResultOfDigitalExpressionXml.class);
}

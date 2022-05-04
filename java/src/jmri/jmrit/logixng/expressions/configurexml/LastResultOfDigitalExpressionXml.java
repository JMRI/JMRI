package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.LastResultOfDigitalExpression;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;

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

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<MaleDigitalExpressionSocket>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        LastResultOfDigitalExpression h = new LastResultOfDigitalExpression(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<MaleDigitalExpressionSocket>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean(), true);
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "expression", null, null, null, null);

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LastResultOfDigitalExpressionXml.class);
}

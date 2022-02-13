package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.expressions.AnalogExpressionAnalogIO;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class AnalogExpressionAnalogIOXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public AnalogExpressionAnalogIOXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        AnalogExpressionAnalogIO p = (AnalogExpressionAnalogIO) o;

        Element element = new Element("AnalogExpressionAnalogIO");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var analogIO = p.getAnalogIO();
        if (analogIO != null) {
            element.addContent(new Element("analogIO").addContent(analogIO.getName()));
        }

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        AnalogExpressionAnalogIO h;
        h = new AnalogExpressionAnalogIO(sys, uname);

        loadCommon(h, shared);

        Element analogIOName = shared.getChild("analogIO");
        if (analogIOName != null) {
            AnalogIO m = InstanceManager.getDefault(AnalogIOManager.class).getNamedBean(analogIOName.getTextTrim());
            if (m != null) h.setAnalogIO(m);
            else h.removeAnalogIO();
        }

        InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogExpressionAnalogIOXml.class);
}

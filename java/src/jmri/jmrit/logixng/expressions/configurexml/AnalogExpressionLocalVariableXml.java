package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.expressions.AnalogExpressionLocalVariable;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectStringXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class AnalogExpressionLocalVariableXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public AnalogExpressionLocalVariableXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        AnalogExpressionLocalVariable p = (AnalogExpressionLocalVariable) o;

        Element element = new Element("AnalogExpressionLocalVariable");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectLocalVariableXml = new LogixNG_SelectStringXml();
        element.addContent(selectLocalVariableXml.store(p.getSelectNamedBean(), "localVariable"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        AnalogExpressionLocalVariable h;
        h = new AnalogExpressionLocalVariable(sys, uname);

        loadCommon(h, shared);

        var selectLocalVariableXml = new LogixNG_SelectStringXml();
        selectLocalVariableXml.load(shared.getChild("localVariable"), h.getSelectNamedBean());

        InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogExpressionLocalVariableXml.class);
}

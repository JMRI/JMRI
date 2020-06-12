package jmri.jmrit.logixng.analog.expressions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.analog.expressions.AnalogExpressionConstant;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class AnalogExpressionConstantXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public AnalogExpressionConstantXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        AnalogExpressionConstant p = (AnalogExpressionConstant) o;

        Element element = new Element("analog-expression-constant");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(new Element("value").addContent(Double.toString(p.getValue())));
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        AnalogExpressionConstant h;
        h = new AnalogExpressionConstant(sys, uname);

        loadCommon(h, shared);

        Element valueElement = shared.getChild("value");
        h.setValue(Double.parseDouble(valueElement.getTextTrim()));

        InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(h);
        return true;
    }
    
    private final static Logger log = LoggerFactory.getLogger(AnalogExpressionConstantXml.class);
}

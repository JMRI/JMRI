package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionPower;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ExpressionPower objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class ExpressionPowerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionPowerXml() {
    }
    
    /**
     * Default implementation for storing the contents of a ExpressionPower
     *
     * @param o Object to store, of type ExpressionPower
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionPower p = (ExpressionPower) o;

        Element element = new Element("ExpressionPower");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        element.addContent(new Element("is_isNot").addContent(p.get_Is_IsNot().name()));
        
        element.addContent(new Element("powerState").addContent(p.getBeanState().name()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionPower h = new ExpressionPower(sys, uname);
        
        loadCommon(h, shared);
        
        Element is_IsNot = shared.getChild("is_isNot");
        if (is_IsNot != null) {
            h.set_Is_IsNot(Is_IsNot_Enum.valueOf(is_IsNot.getTextTrim()));
        }
        
        Element powerState = shared.getChild("powerState");
        if (powerState != null) {
            h.setBeanState(ExpressionPower.PowerState.valueOf(powerState.getTextTrim()));
        }
        
        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionPowerXml.class);
}

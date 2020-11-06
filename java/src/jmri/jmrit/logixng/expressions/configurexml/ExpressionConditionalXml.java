package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrit.logixng.expressions.ExpressionConditional;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ExpressionConditionalXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionConditionalXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionConditional p = (ExpressionConditional) o;

//        if (p.getLightName() == null) throw new RuntimeException("aaaaa");
        
        Element element = new Element("expression-conditional");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        Conditional conditional = p.getConditional();
        if (conditional != null) {
            element.addContent(new Element("conditional").addContent(conditional.getSystemName()));
        }
        
        element.addContent(new Element("is_isNot").addContent(p.get_Is_IsNot().name()));
        element.addContent(new Element("conditionalState").addContent(p.getConditionalState().name()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionConditional h = new ExpressionConditional(sys, uname);

        loadCommon(h, shared);

        Element conditionalName = shared.getChild("conditional");
        if (conditionalName != null) {
            Conditional t = InstanceManager.getDefault(ConditionalManager.class).getBySystemName(conditionalName.getTextTrim());
            if (t != null) h.setConditional(t);
            else h.removeConditional();
        }

        Element is_IsNot = shared.getChild("is_isNot");
        if (is_IsNot != null) {
            h.set_Is_IsNot(Is_IsNot_Enum.valueOf(is_IsNot.getTextTrim()));
        }

        Element type = shared.getChild("conditionalState");
        if (type != null) {
            h.setConditionalState(ExpressionConditional.ConditionalState.valueOf(type.getTextTrim()));
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionLightXml.class);
}

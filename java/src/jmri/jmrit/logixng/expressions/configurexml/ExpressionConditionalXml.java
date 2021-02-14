package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionConditional;
import jmri.jmrit.logixng.util.parser.ParserException;

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
        
        Element element = new Element("ExpressionConditional");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        NamedBeanHandle conditional = p.getConditional();
        if (conditional != null) {
            element.addContent(new Element("conditional").addContent(conditional.getName()));
        }
        
        element.addContent(new Element("addressing").addContent(p.getAddressing().name()));
        element.addContent(new Element("reference").addContent(p.getReference()));
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("formula").addContent(p.getFormula()));
        
        element.addContent(new Element("is_isNot").addContent(p.get_Is_IsNot().name()));
        
        element.addContent(new Element("stateAddressing").addContent(p.getStateAddressing().name()));
        element.addContent(new Element("conditionalState").addContent(p.getConditionalState().name()));
        element.addContent(new Element("stateReference").addContent(p.getStateReference()));
        element.addContent(new Element("stateLocalVariable").addContent(p.getStateLocalVariable()));
        element.addContent(new Element("stateFormula").addContent(p.getStateFormula()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionConditional h = new ExpressionConditional(sys, uname);

        loadCommon(h, shared);

        Element conditionalName = shared.getChild("conditional");
        if (conditionalName != null) {
            Conditional t = InstanceManager.getDefault(ConditionalManager.class).getConditional(conditionalName.getTextTrim());
            if (t != null) h.setConditional(t);
            else h.removeConditional();
        }

        try {
            Element elem = shared.getChild("addressing");
            if (elem != null) {
                h.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }
            
            elem = shared.getChild("reference");
            if (elem != null) h.setReference(elem.getTextTrim());
            
            elem = shared.getChild("localVariable");
            if (elem != null) h.setLocalVariable(elem.getTextTrim());
            
            elem = shared.getChild("formula");
            if (elem != null) h.setFormula(elem.getTextTrim());
            
            
            Element is_IsNot = shared.getChild("is_isNot");
            if (is_IsNot != null) {
                h.set_Is_IsNot(Is_IsNot_Enum.valueOf(is_IsNot.getTextTrim()));
            }
            
            
            elem = shared.getChild("stateAddressing");
            if (elem != null) {
                h.setStateAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }
            
            Element conditionalState = shared.getChild("conditionalState");
            if (conditionalState != null) {
                h.setConditionalState(ExpressionConditional.ConditionalState.valueOf(conditionalState.getTextTrim()));
            }
            
            elem = shared.getChild("stateReference");
            if (elem != null) h.setStateReference(elem.getTextTrim());
            
            elem = shared.getChild("stateLocalVariable");
            if (elem != null) h.setStateLocalVariable(elem.getTextTrim());
            
            elem = shared.getChild("stateFormula");
            if (elem != null) h.setStateFormula(elem.getTextTrim());
            
        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionLightXml.class);
}

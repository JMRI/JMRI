package jmri.jmrit.logixng.digital.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.digital.expressions.ExpressionMemory;

import org.jdom2.Element;

import jmri.jmrit.logixng.Is_IsNot_Enum;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ExpressionMemoryXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionMemoryXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionMemory p = (ExpressionMemory) o;

        Element element = new Element("expression-memory");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        NamedBeanHandle memory = p.getMemory();
        if (memory != null) {
            element.addContent(new Element("memory").addContent(memory.getName()));
        }
        NamedBeanHandle otherMemory = p.getOtherMemory();
        if (otherMemory != null) {
            element.addContent(new Element("other-memory").addContent(otherMemory.getName()));
        }
        String constantValue = p.getConstantValue();
        if (constantValue != null) {
            element.addContent(new Element("constant").addContent(constantValue));
        }
        element.addContent(new Element("memory-operation").addContent(p.getMemoryOperation().name()));
        element.addContent(new Element("compare-to").addContent(p.getCompareTo().name()));
        element.setAttribute("case-sensitive", p.getCaseInsensitive() ? "yes" : "no");

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionMemory h = new ExpressionMemory(sys, uname);

        loadCommon(h, shared);

        Element memoryName = shared.getChild("memory");
        if (memoryName != null) {
            Memory m = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
            if (m != null) h.setMemory(m);
            else h.removeMemory();
        }

        Element otherMemoryName = shared.getChild("other-memory");
        if (otherMemoryName != null) {
            h.setOtherMemory(InstanceManager.getDefault(MemoryManager.class).getMemory(otherMemoryName.getTextTrim()));
        }

        Element constant = shared.getChild("constant");
        if (constant != null) {
            h.setConstantValue(constant.getText());
        }

        Element memoryOperation = shared.getChild("memory-operation");
        if (memoryOperation != null) {
            h.setMemoryOperation(ExpressionMemory.MemoryOperation.valueOf(memoryOperation.getTextTrim()));
        }

        Element compareTo = shared.getChild("compare-to");
        if (compareTo != null) {
            h.setCompareTo(ExpressionMemory.CompareTo.valueOf(compareTo.getTextTrim()));
        }

        h.setCaseInsensitive("yes".equals(shared.getAttributeValue("case-sensitive", "no")));

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionTurnoutXml.class);
}

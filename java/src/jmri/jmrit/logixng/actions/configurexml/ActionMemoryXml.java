package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBeanHandle;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.actions.ActionMemory;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionMemoryXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionMemoryXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionMemory p = (ActionMemory) o;

        Element element = new Element("ActionMemory");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        NamedBeanHandle<Memory> memory = p.getMemory();
        if (memory != null) {
            element.addContent(new Element("memory").addContent(memory.getName()));
        }
        
        element.addContent(new Element("addressing").addContent(p.getAddressing().name()));
        element.addContent(new Element("reference").addContent(p.getReference()));
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("formula").addContent(p.getFormula()));
        
        NamedBeanHandle<Memory> otherMemoryName = p.getOtherMemory();
        if (otherMemoryName != null) {
            element.addContent(new Element("otherMemory").addContent(otherMemoryName.getName()));
        }
        
        element.addContent(new Element("memoryOperation").addContent(p.getMemoryOperation().name()));
        
        element.addContent(new Element("otherConstant").addContent(p.getConstantValue()));
        element.addContent(new Element("otherVariable").addContent(p.getOtherLocalVariable()));
        element.addContent(new Element("otherFormula").addContent(p.getOtherFormula()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionMemory h = new ActionMemory(sys, uname);

        loadCommon(h, shared);
        
        
        Element memoryName = shared.getChild("memory");
        if (memoryName != null) {
            Memory t = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
            if (t != null) h.setMemory(t);
            else h.removeMemory();
        }

        Element otherMemoryName = shared.getChild("otherMemory");
        if (otherMemoryName != null) {
            Memory t = InstanceManager.getDefault(MemoryManager.class).getMemory(otherMemoryName.getTextTrim());
            if (t != null) h.setOtherMemory(t);
            else h.removeOtherMemory();
        }

        Element queryType = shared.getChild("memoryOperation");
        if (queryType != null) {
            try {
                h.setMemoryOperation(ActionMemory.MemoryOperation.valueOf(queryType.getTextTrim()));
            } catch (ParserException e) {
                log.error("cannot set memory operation: " + queryType.getTextTrim(), e);
            }
        }

        
        Element elemAddressing = shared.getChild("addressing");
        
        // Temporary solution for handling change in the xml file.
        // Remove this when JMRI 4.24 is released.
        if (elemAddressing == null) {
            Element constant = shared.getChild("constant");
            if (constant != null) {
                h.setOtherConstantValue(constant.getTextTrim());
            }

            Element variable = shared.getChild("variable");
            if (variable != null) {
                h.setOtherLocalVariable(variable.getTextTrim());
            }

            Element formula = shared.getChild("formula");
            if (formula != null) {
                try {
                    h.setOtherFormula(formula.getTextTrim());
                } catch (ParserException e) {
                    log.error("cannot set data: " + formula.getTextTrim(), e);
                }
            }
        } else {
            try {
                h.setAddressing(NamedBeanAddressing.valueOf(elemAddressing.getTextTrim()));
                
                Element elem = shared.getChild("reference");
                if (elem != null) h.setReference(elem.getTextTrim());
                
                elem = shared.getChild("localVariable");
                if (elem != null) h.setLocalVariable(elem.getTextTrim());
                
                elem = shared.getChild("formula");
                if (elem != null) h.setFormula(elem.getTextTrim());
                
                
//                elem = shared.getChild("otherAddressing");
//                if (elem != null) {
//                    h.setOtherAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
//                }
                
//                Element memoryOperation = shared.getChild("lightState");
//                if (memoryOperation != null) {
//                    h.setMemoryOperation(ActionMemory.MemoryOperation.valueOf(memoryOperation.getTextTrim()));
//                }
                
//                elem = shared.getChild("otherReference");
//                if (elem != null) h.setOtherReference(elem.getTextTrim());
                
                Element constant = shared.getChild("otherConstant");
                if (constant != null) {
                    h.setOtherConstantValue(constant.getTextTrim());
                }
                
                elem = shared.getChild("otherVariable");
                if (elem != null) h.setOtherLocalVariable(elem.getTextTrim());
                
                elem = shared.getChild("otherFormula");
                if (elem != null) h.setOtherFormula(elem.getTextTrim());
                
            } catch (ParserException e) {
                throw new JmriConfigureXmlException(e);
            }
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionMemoryXml.class);
}

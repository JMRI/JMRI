package jmri.jmrit.display.logixng.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.display.logixng.ActionPositionable;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionPositionable objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class ActionPositionableXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionPositionableXml() {
    }
    
    /**
     * Default implementation for storing the contents of a ActionPositionable
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionPositionable p = (ActionPositionable) o;

        Element element = new Element("DisplayActionPositionable");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        String editorName = p.getEditorName();
        if (editorName != null) {
            element.addContent(new Element("editorName").addContent(editorName));
        }
        
        String positionableName = p.getPositionableName();
        if (positionableName != null) {
            element.addContent(new Element("positionableName").addContent(positionableName));
        }
        
        element.addContent(new Element("addressing").addContent(p.getAddressing().name()));
        element.addContent(new Element("reference").addContent(p.getReference()));
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("formula").addContent(p.getFormula()));
        
        element.addContent(new Element("stateAddressing").addContent(p.getStateAddressing().name()));
        element.addContent(new Element("operation").addContent(p.getOperation().name()));
        element.addContent(new Element("stateReference").addContent(p.getStateReference()));
        element.addContent(new Element("stateLocalVariable").addContent(p.getStateLocalVariable()));
        element.addContent(new Element("stateFormula").addContent(p.getStateFormula()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionPositionable h = new ActionPositionable(sys, uname);
        
        loadCommon(h, shared);
        
        Element elem = shared.getChild("editorName");
        if (elem != null) {
            h.setEditor(elem.getTextTrim());
        }
        
        elem = shared.getChild("positionableName");
        if (elem != null) {
            h.setPositionable(elem.getTextTrim());
        }
        
        try {
            elem = shared.getChild("addressing");
            if (elem != null) {
                h.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }
            
            elem = shared.getChild("reference");
            if (elem != null) h.setReference(elem.getTextTrim());
            
            elem = shared.getChild("localVariable");
            if (elem != null) h.setLocalVariable(elem.getTextTrim());
            
            elem = shared.getChild("formula");
            if (elem != null) h.setFormula(elem.getTextTrim());
            
            
            elem = shared.getChild("stateAddressing");
            if (elem != null) {
                h.setStateAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }
            
            Element isControlling = shared.getChild("operation");
            if (isControlling != null) {
                h.setOperation(ActionPositionable.Operation.valueOf(isControlling.getTextTrim()));
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
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionPositionableXml.class);
}

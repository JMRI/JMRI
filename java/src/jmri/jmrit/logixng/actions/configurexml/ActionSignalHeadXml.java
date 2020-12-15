package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.actions.ActionSignalHead;
import jmri.jmrit.logixng.actions.ActionSignalMast;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionSignalHeadXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionSignalHeadXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionSignalHead p = (ActionSignalHead) o;

        Element element = new Element("action-signalhead");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        NamedBeanHandle<SignalHead> signalHead = p.getSignalHead();
        if (signalHead != null) {
            element.addContent(new Element("signalHead").addContent(signalHead.getName()));
        }
        
        element.addContent(new Element("addressing").addContent(p.getAddressing().name()));
        element.addContent(new Element("reference").addContent(p.getReference()));
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("formula").addContent(p.getFormula()));
        
        element.addContent(new Element("operationAddressing").addContent(p.getOperationAddressing().name()));
        element.addContent(new Element("operationType").addContent(p.getOperationType().name()));
        element.addContent(new Element("operationReference").addContent(p.getOperationReference()));
        element.addContent(new Element("operationLocalVariable").addContent(p.getOperationLocalVariable()));
        element.addContent(new Element("operationFormula").addContent(p.getOperationFormula()));
        
        element.addContent(new Element("appearanceAddressing").addContent(p.getAppearanceAddressing().name()));
        element.addContent(new Element("appearance").addContent(Integer.toString(p.getAppearance())));
        element.addContent(new Element("appearanceReference").addContent(p.getAppearanceReference()));
        element.addContent(new Element("appearanceLocalVariable").addContent(p.getAppearanceLocalVariable()));
        element.addContent(new Element("appearanceFormula").addContent(p.getAppearanceFormula()));
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionSignalHead h = new ActionSignalHead(sys, uname);

        loadCommon(h, shared);

        Element signalHeadName = shared.getChild("signalHead");
        if (signalHeadName != null) {
            SignalHead signalHead = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalHeadName.getTextTrim());
            if (signalHead != null) h.setSignalHead(signalHead);
            else h.removeSignalHead();
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
            
            
            elem = shared.getChild("operationAddressing");
            if (elem != null) {
                h.setOperationAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }
            
            Element queryType = shared.getChild("operationType");
            if (queryType != null) {
                h.setOperationType(ActionSignalHead.OperationType.valueOf(queryType.getTextTrim()));
            }
            
            elem = shared.getChild("operationReference");
            if (elem != null) h.setOperationReference(elem.getTextTrim());
            
            elem = shared.getChild("operationLocalVariable");
            if (elem != null) h.setOperationLocalVariable(elem.getTextTrim());
            
            elem = shared.getChild("operationFormula");
            if (elem != null) h.setOperationFormula(elem.getTextTrim());
            
            
            elem = shared.getChild("apperanceAddressing");
            if (elem != null) {
                h.setAppearanceAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }
            
            Element apperanceElement = shared.getChild("appearance");
            if (apperanceElement != null) {
                try {
                    int apperance = Integer.parseInt(apperanceElement.getTextTrim());
                    h.setAppearance(apperance);
                } catch (NumberFormatException e) {
                    log.error("cannot parse apperance: " + apperanceElement.getTextTrim(), e);
                }
            }
            
            elem = shared.getChild("apperanceReference");
            if (elem != null) h.setAppearanceReference(elem.getTextTrim());
            
            elem = shared.getChild("apperanceLocalVariable");
            if (elem != null) h.setAppearanceLocalVariable(elem.getTextTrim());
            
            elem = shared.getChild("apperanceFormula");
            if (elem != null) h.setAppearanceFormula(elem.getTextTrim());
            
        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalHeadXml.class);
}

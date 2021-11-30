package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.actions.ActionSound;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionSound objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class ActionSoundXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionSoundXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalMast
     *
     * @param o Object to store, of type TripleLightSignalMast
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionSound p = (ActionSound) o;

        Element element = new Element("ActionSound");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(new Element("operationAddressing").addContent(p.getOperationAddressing().name()));
        element.addContent(new Element("operationType").addContent(p.getOperation().name()));
        element.addContent(new Element("operationReference").addContent(p.getOperationReference()));
        element.addContent(new Element("operationLocalVariable").addContent(p.getOperationLocalVariable()));
        element.addContent(new Element("operationFormula").addContent(p.getOperationFormula()));
        
        element.addContent(new Element("soundAddressing").addContent(p.getSoundAddressing().name()));
        element.addContent(new Element("sound").addContent(p.getSound()));
        element.addContent(new Element("soundReference").addContent(p.getSoundReference()));
        element.addContent(new Element("soundLocalVariable").addContent(p.getSoundLocalVariable()));
        element.addContent(new Element("soundFormula").addContent(p.getSoundFormula()));
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionSound h = new ActionSound(sys, uname);

        loadCommon(h, shared);

        try {
            Element elem = shared.getChild("operationAddressing");
            if (elem != null) {
                h.setOperationAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }
            
            Element queryType = shared.getChild("operationType");
            if (queryType != null) {
                h.setOperation(ActionSound.Operation.valueOf(queryType.getTextTrim()));
            }
            
            elem = shared.getChild("operationReference");
            if (elem != null) h.setOperationReference(elem.getTextTrim());
            
            elem = shared.getChild("operationLocalVariable");
            if (elem != null) h.setOperationLocalVariable(elem.getTextTrim());
            
            elem = shared.getChild("operationFormula");
            if (elem != null) h.setOperationFormula(elem.getTextTrim());
            
            
            elem = shared.getChild("soundAddressing");
            if (elem != null) {
                h.setSoundAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }
            
            Element soundElement = shared.getChild("sound");
            if (soundElement != null) {
                try {
                    h.setSound(soundElement.getText());
                } catch (NumberFormatException e) {
                    log.error("cannot parse sound: " + soundElement.getTextTrim(), e);
                }
            }
            
            elem = shared.getChild("soundReference");
            if (elem != null) h.setSoundReference(elem.getTextTrim());
            
            elem = shared.getChild("soundLocalVariable");
            if (elem != null) h.setSoundLocalVariable(elem.getTextTrim());
            
            elem = shared.getChild("soundFormula");
            if (elem != null) h.setSoundFormula(elem.getTextTrim());
            
        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSoundXml.class);
}

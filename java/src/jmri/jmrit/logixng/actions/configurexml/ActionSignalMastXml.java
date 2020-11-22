package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ActionSignalMast;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionSignalMastXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionSignalMastXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalMast
     *
     * @param o Object to store, of type TripleLightSignalMast
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionSignalMast p = (ActionSignalMast) o;

//        if (p.getLightName() == null) throw new RuntimeException("aaaaa");
        
        Element element = new Element("action-signalmast");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        NamedBeanHandle<SignalMast> signalMast = p.getSignalMast();
        if (signalMast != null) {
            element.addContent(new Element("signalMast").addContent(signalMast.getName()));
        }
        
        element.addContent(new Element("operationType").addContent(p.getOperationType().name()));
        
        element.addContent(new Element("aspect").addContent(p.getAspect()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionSignalMast h = new ActionSignalMast(sys, uname);

        loadCommon(h, shared);

        Element signalMastName = shared.getChild("signalMast");
        if (signalMastName != null) {
            SignalMast signalMast = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(signalMastName.getTextTrim());
            if (signalMast != null) h.setSignalMast(signalMast);
            else h.removeSignalMast();
        }

        Element queryType = shared.getChild("operationType");
        if (queryType != null) {
            h.setOperationType(ActionSignalMast.OperationType.valueOf(queryType.getTextTrim()));
        }
        
        Element apperanceElement = shared.getChild("aspect");
        if (apperanceElement != null) {
            h.setAspect(apperanceElement.getTextTrim());
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLightXml.class);
}

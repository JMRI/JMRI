package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ShutdownComputer;

import org.jdom2.Element;

import jmri.jmrit.logixng.DigitalActionBean;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ShutdownComputerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ShutdownComputerXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleSensorSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ShutdownComputer p = (ShutdownComputer) o;
        
        Element element = new Element("ShutdownComputer");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);
        
        element.addContent(new Element("operation").addContent(p.getOperation().name()));
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        
        ShutdownComputer h = new ShutdownComputer(sys, uname);
        
        loadCommon(h, shared);
        
        Element operation = shared.getChild("operation");
        if (operation != null) {
            h.setOperation(ShutdownComputer.Operation.valueOf(operation.getTextTrim()));
        }
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShutdownComputerXml.class);
}

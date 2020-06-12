package jmri.jmrit.logixng.digital.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.digital.actions.ShutdownComputer;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

        Element element = new Element("shutdown-computer");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        element.setAttribute("seconds", Integer.toString(p.getSeconds()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        
        int seconds = 0;
        try {
            seconds = shared.getAttribute("seconds").getIntValue();
        } catch (DataConversionException ex) {
            log.error("seconds attribute is not an integer", ex);
        }
        
        DigitalActionBean h = new ShutdownComputer(sys, uname, seconds);

        loadCommon(h, shared);

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
    private final static Logger log = LoggerFactory.getLogger(ShutdownComputerXml.class);
}

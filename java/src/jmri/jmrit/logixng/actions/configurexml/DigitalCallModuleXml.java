package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.ModuleManager;
import jmri.jmrit.logixng.actions.DigitalCallModule;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class DigitalCallModuleXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DigitalCallModuleXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        DigitalCallModule p = (DigitalCallModule) o;

        Element element = new Element("call-module");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        NamedBeanHandle<Module> module = p.getModule();
        if (module != null) {
            element.addContent(new Element("module").addContent(module.getName()));
        }
        
//        element.addContent(new Element("lightState").addContent(p.getLightState().name()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        DigitalCallModule h = new DigitalCallModule(sys, uname);

        loadCommon(h, shared);

        Element moduleName = shared.getChild("module");
        if (moduleName != null) {
            Module t = InstanceManager.getDefault(ModuleManager.class).getModule(moduleName.getTextTrim());
            if (t != null) h.setModule(t);
            else h.removeModule();
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLightXml.class);
}

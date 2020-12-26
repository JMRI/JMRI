package jmri.jmrit.logixng.implementation.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.ModuleManager;
import jmri.jmrit.logixng.implementation.DefaultModule;

import org.jdom2.Element;

/**
 * Handle XML configuration for DefaultModule objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public class DefaultModuleXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DefaultModuleXml() {
    }
    
    /**
     * Default implementation for storing the contents of a DefaultModule
     *
     * @param o Object to store, of type DefaultModule
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        DefaultModule p = (DefaultModule) o;

        Element element = new Element("module");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);
        
        element.addContent(new Element("rootSocketType").addContent(p.getRootSocketType().getName()));
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        
        String rootSocketTypeName = shared.getChild("rootSocketType").getTextTrim();
        
        FemaleSocketManager.SocketType socketType =
                InstanceManager.getDefault(FemaleSocketManager.class)
                        .getSocketTypeByType(rootSocketTypeName);
        
        Module h = InstanceManager.getDefault(ModuleManager.class)
                .createModule(sys, uname, socketType);
        
        loadCommon(h, shared);
        
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultModuleXml.class);
}

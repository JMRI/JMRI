package jmri.jmrit.logixng.actions.configurexml;

import java.util.List;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module.ParameterData;
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

        Element element = new Element("CallModule");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        NamedBeanHandle<Module> module = p.getModule();
        if (module != null) {
            element.addContent(new Element("module").addContent(module.getName()));
        }
        
        Element parameters = new Element("Parameters");
        for (ParameterData pd : p.getParameterData()) {
            Element elementParameter = new Element("Parameter");
            elementParameter.addContent(new Element("name").addContent(pd._name));
            elementParameter.addContent(new Element("initalValueType").addContent(pd._initialValueType.name()));
            elementParameter.addContent(new Element("initialValueData").addContent(pd._initialValueData));
            elementParameter.addContent(new Element("returnValueType").addContent(pd._returnValueType.name()));
            elementParameter.addContent(new Element("returnValueData").addContent(pd._returnValueData));
            parameters.addContent(elementParameter);
        }
        element.addContent(parameters);

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
        
        List<Element> parameterList = shared.getChild("Parameters").getChildren();  // NOI18N
        log.debug("Found " + parameterList.size() + " parameters");  // NOI18N

        for (Element e : parameterList) {
            Element elementName = e.getChild("name");
            
            SymbolTable.InitialValueType initialValueType = null;
            Element elementType = e.getChild("initalValueType");
            if (elementType != null) {
                initialValueType = SymbolTable.InitialValueType.valueOf(elementType.getTextTrim());
            }
            
            Element elementInitialValueData = e.getChild("initialValueData");
            
            Module.ReturnValueType returnValueType = null;
            elementType = e.getChild("returnValueType");
            if (elementType != null) {
                returnValueType = Module.ReturnValueType.valueOf(elementType.getTextTrim());
            }
            
            Element elementReturnValueData = e.getChild("returnValueData");
            
            if (elementName == null) throw new IllegalArgumentException("Element 'name' does not exists");
            if (initialValueType == null) throw new IllegalArgumentException("Element 'initalValueType' does not exists");
            if (elementInitialValueData == null) throw new IllegalArgumentException("Element 'initialValueData' does not exists");
            if (returnValueType == null) throw new IllegalArgumentException("Element 'returnValueType' does not exists");
            if (elementReturnValueData == null) throw new IllegalArgumentException("Element 'returnValueData' does not exists");
            
            h.addParameter(elementName.getTextTrim(),
                    initialValueType, elementInitialValueData.getTextTrim(),
                    returnValueType, elementReturnValueData.getTextTrim());
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigitalCallModuleXml.class);
}

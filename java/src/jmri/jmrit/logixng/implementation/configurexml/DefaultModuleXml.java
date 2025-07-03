package jmri.jmrit.logixng.implementation.configurexml;

import java.util.List;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module.Parameter;
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

        // Don't store the module if it's empty and if "store if empty" is false.
        if (!p.isStoreIfEmpty() && !p.getRootSocket().isConnected()) {
            return null;
        }

        Element element = new Element("Module");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(new Element("rootSocketType").addContent(p.getRootSocketType().getName()));

        if (!p.isVisible()) {
            element.addContent(new Element("isVisible").addContent(p.isVisible() ? "yes" : "no"));
        }

        if (!p.isStoreIfEmpty()) {
            element.addContent(new Element("storeIfEmpty").addContent(p.isStoreIfEmpty() ? "yes" : "no"));
        }

        Element elementParameters = new Element("Parameters");
        for (Parameter data : p.getParameters()) {
            Element elementParameter = new Element("Parameter");
            elementParameter.addContent(new Element("name").addContent(data.getName()));
            elementParameter.addContent(new Element("isInput").addContent(data.isInput() ? "yes" : "no"));
            elementParameter.addContent(new Element("isOutput").addContent(data.isOutput() ? "yes" : "no"));
            elementParameters.addContent(elementParameter);
        }
        element.addContent(elementParameters);

        Element e2 = new Element("RootSocket");
        e2.addContent(new Element("socketName").addContent(p.getChild(0).getName()));
        MaleSocket socket = p.getRootSocket().getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

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

        DefaultModule h = (DefaultModule) InstanceManager.getDefault(ModuleManager.class)
                .createModule(sys, uname, socketType);

        // The error handling module might already exist. If so, get the existing module.
        if (h == null && LogixNG_Manager.ERROR_HANDLING_MODULE_NAME.equals(sys)) {
            h = (DefaultModule) InstanceManager.getDefault(ModuleManager.class).getBySystemName(sys);
        }

        if (h == null) {
            log.error("Module {} cannot be loaded", sys);
            return false;
        }

        loadCommon(h, shared);

        List<Element> parameterList = shared.getChild("Parameters").getChildren();  // NOI18N
        log.debug("Found {} parameters", parameterList.size() );  // NOI18N

        for (Element e : parameterList) {
            Element elementName = e.getChild("name");

            boolean isInput = "yes".equals(e.getChild("isInput").getTextTrim());
            boolean isOutput = "yes".equals(e.getChild("isOutput").getTextTrim());

            h.addParameter(elementName.getTextTrim(), isInput, isOutput);
        }

        Element socketName = shared.getChild("RootSocket").getChild("socketName");
        h.getChild(0).setName(socketName.getTextTrim());
        Element socketSystemName = shared.getChild("RootSocket").getChild("systemName");
        if (socketSystemName != null) {
            h.setSocketSystemName(socketSystemName.getTextTrim());
        }

        Element isVisibleElement = shared.getChild("isVisible");
        if (isVisibleElement != null) {
            h.setVisible("yes".equals(isVisibleElement.getTextTrim()));
        }

        Element storeIfEmptyElement = shared.getChild("storeIfEmpty");
        if (storeIfEmptyElement != null) {
            h.setStoreIfEmpty("yes".equals(storeIfEmptyElement.getTextTrim()));
        }

        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultModuleXml.class);
}

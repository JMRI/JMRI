package jmri.jmrit.logixng.implementation.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.GlobalVariableManager;
import jmri.jmrit.logixng.implementation.DefaultGlobalVariable;

import org.jdom2.Element;

/**
 * Handle XML configuration for DefaultGlobalVariable objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public class DefaultGlobalVariableXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DefaultGlobalVariableXml() {
    }

    /**
     * Default implementation for storing the contents of a DefaultGlobalVariable
     *
     * @param o Object to store, of type DefaultGlobalVariable
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        DefaultGlobalVariable p = (DefaultGlobalVariable) o;

        Element element = new Element("GlobalVariable");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(new Element("initialValueType").addContent(p.getInitialValueType().name()));

        if (p.getInitialValueData() != null) {
            element.addContent(new Element("initialValueData").addContent(p.getInitialValueData()));
        }

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode)
            throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);

        DefaultGlobalVariable h = (DefaultGlobalVariable) InstanceManager.getDefault(GlobalVariableManager.class)
                .createGlobalVariable(sys, uname);

        loadCommon(h, shared);

        String initialValueType = shared.getChild("initialValueType").getTextTrim();
        h.setInitialValueType(SymbolTable.InitialValueType.valueOf(initialValueType));

        Element elementInitialValueData = shared.getChild("initialValueData");
        if (elementInitialValueData != null) {
            h.setInitialValueData(elementInitialValueData.getTextTrim());
        }

        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultGlobalVariableXml.class);
}

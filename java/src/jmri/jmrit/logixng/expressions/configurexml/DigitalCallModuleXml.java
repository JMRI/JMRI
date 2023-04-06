package jmri.jmrit.logixng.expressions.configurexml;

import java.util.List;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module.ParameterData;
import jmri.jmrit.logixng.expressions.DigitalCallModule;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ExpressionLightXml objects.
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

        Element element = new Element("CallDigitalExpressionModule");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Module>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        Element parameters = new Element("Parameters");
        for (ParameterData pd : p.getParameterData()) {
            Element elementParameter = new Element("Parameter");
            elementParameter.addContent(new Element("name").addContent(pd._name));
            elementParameter.addContent(new Element("initialValueType").addContent(pd._initialValueType.name()));
            elementParameter.addContent(new Element("initialValueData").addContent(pd._initialValueData));
            elementParameter.addContent(new Element("returnValueType").addContent(pd._returnValueType.name()));
            elementParameter.addContent(new Element("returnValueData").addContent(pd._returnValueData));
            parameters.addContent(elementParameter);
        }
        element.addContent(parameters);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        DigitalCallModule h = new DigitalCallModule(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Module>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "module", null, null, null, null);

        List<Element> parameterList = shared.getChild("Parameters").getChildren();  // NOI18N
        log.debug("Found {} parameters", parameterList.size() );  // NOI18N

        for (Element e : parameterList) {
            Element elementName = e.getChild("name");

            SymbolTable.InitialValueType initialValueType = null;
            Element elementType = e.getChild("initialValueType");
            if (elementType == null) {
                elementType = e.getChild("initalValueType");    // Spelling error in previous versions of JMRI
            }
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
            if (initialValueType == null) throw new IllegalArgumentException("Element 'initialValueType' does not exists");
            if (elementInitialValueData == null) throw new IllegalArgumentException("Element 'initialValueData' does not exists");
            if (returnValueType == null) throw new IllegalArgumentException("Element 'returnValueType' does not exists");
            if (elementReturnValueData == null) throw new IllegalArgumentException("Element 'returnValueData' does not exists");

            h.addParameter(elementName.getTextTrim(),
                    initialValueType, elementInitialValueData.getTextTrim(),
                    returnValueType, elementReturnValueData.getTextTrim());
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigitalCallModuleXml.class);
}

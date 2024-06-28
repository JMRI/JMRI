package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.JsonDecode;

import org.jdom2.Element;

/**
 * Handle XML configuration for JsonDecode objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2024
 */
public class JsonDecodeXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public JsonDecodeXml() {
    }

    /**
     * Default implementation for storing the contents of a JsonDecode
     *
     * @param o Object to store, of type JsonDecode
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        JsonDecode p = (JsonDecode) o;

        Element element = new Element("JsonDecode");   // NOI18N
        element.setAttribute("class", this.getClass().getName());   // NOI18N
        element.addContent(new Element("systemName").addContent(p.getSystemName()));    // NOI18N

        storeCommon(p, element);

        String jsonVariableName = p.getJsonLocalVariable();
        if (jsonVariableName != null) {
            element.addContent(new Element("jsonVariable").addContent(jsonVariableName));   // NOI18N
        }

        String resultVariableName = p.getResultLocalVariable();
        if (resultVariableName != null) {
            element.addContent(new Element("resultVariable").addContent(resultVariableName));   // NOI18N
        }

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        JsonDecode h = new JsonDecode(sys, uname);

        loadCommon(h, shared);

        Element jsonVariableName = shared.getChild("jsonVariable"); // NOI18N
        if (jsonVariableName != null) {
            h.setJsonLocalVariable(jsonVariableName.getTextTrim());
        }

        Element resultVariableName = shared.getChild("resultVariable"); // NOI18N
        if (resultVariableName != null) {
            h.setResultLocalVariable(resultVariableName.getTextTrim());
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonDecodeXml.class);
}

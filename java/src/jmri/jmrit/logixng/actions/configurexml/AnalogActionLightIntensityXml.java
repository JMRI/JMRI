package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AnalogActionLightIntensity;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;


/**
 * Handle XML configuration for AnalogActionLightIntensity objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class AnalogActionLightIntensityXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public AnalogActionLightIntensityXml() {
    }

    /**
     * Default implementation for storing the contents of a AnalogActionLightIntensity
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        AnalogActionLightIntensity p = (AnalogActionLightIntensity) o;

        Element element = new Element("AnalogActionLightIntensity");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var light = p.getLight();
        if (light != null) {
            element.addContent(new Element("variableLight").addContent(light.getName()));
        }

        element.addContent(new Element("addressing").addContent(p.getAddressing().name()));
        element.addContent(new Element("reference").addContent(p.getReference()));
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("formula").addContent(p.getFormula()));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {

        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        AnalogActionLightIntensity h = new AnalogActionLightIntensity(sys, uname);

        loadCommon(h, shared);

        Element lightName = shared.getChild("variableLight");
        if (lightName != null) {
            VariableLight t = InstanceManager.getDefault(VariableLightManager.class)
                    .getNamedBean(lightName.getTextTrim());
            if (t != null) h.setLight(t);
            else h.removeLight();
        }

        try {
            Element elem = shared.getChild("addressing");
            if (elem != null) {
                h.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild("reference");
            if (elem != null) h.setReference(elem.getTextTrim());

            elem = shared.getChild("localVariable");
            if (elem != null) h.setLocalVariable(elem.getTextTrim());

            elem = shared.getChild("formula");
            if (elem != null) h.setFormula(elem.getTextTrim());

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        InstanceManager.getDefault(AnalogActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogActionLightIntensityXml.class);
}

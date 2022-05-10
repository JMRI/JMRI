package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.actions.ActionLight;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionLightXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionLightXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionLight p = (ActionLight) o;

        Element element = new Element("ActionLight");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Light>();
        var selectEnumXml = new LogixNG_SelectEnumXml<ActionLight.LightState>();

        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));
        element.addContent(selectEnumXml.store(p.getSelectEnum(), "state"));

        element.addContent(new Element("dataAddressing").addContent(p.getDataAddressing().name()));
        element.addContent(new Element("dataReference").addContent(p.getDataReference()));
        element.addContent(new Element("dataLocalVariable").addContent(p.getDataLocalVariable()));
        element.addContent(new Element("dataFormula").addContent(p.getDataFormula()));

        if (p.getLightValue() > 0) {
            element.addContent(new Element("lightValue").addContent(Integer.toString(p.getLightValue())));
        }

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionLight h = new ActionLight(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Light>();
        var selectEnumXml = new LogixNG_SelectEnumXml<ActionLight.LightState>();

        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "light");

        selectEnumXml.load(shared.getChild("state"), h.getSelectEnum());
        selectEnumXml.loadLegacy(
                shared, h.getSelectEnum(),
                "stateAddressing",
                "lightState",
                "stateReference",
                "stateLocalVariable",
                "stateFormula");

        try {
            Element elem = shared.getChild("dataAddressing");
            if (elem != null) {
                h.setDataAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild("dataReference");
            if (elem != null) h.setDataReference(elem.getTextTrim());

            elem = shared.getChild("dataLocalVariable");
            if (elem != null) h.setDataLocalVariable(elem.getTextTrim());

            elem = shared.getChild("dataFormula");
            if (elem != null) h.setDataFormula(elem.getTextTrim());


            elem = shared.getChild("lightValue");
            if (elem != null) {
                try {
                    h.setLightValue(Integer.parseInt(elem.getTextTrim()));
                } catch (NumberFormatException ex) {
                    h.setLightValue(0);
                }
            }

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLightXml.class);
}

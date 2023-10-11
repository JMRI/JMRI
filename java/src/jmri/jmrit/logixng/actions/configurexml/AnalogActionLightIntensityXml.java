package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AnalogActionLightIntensity;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
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

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<VariableLight>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {

        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        AnalogActionLightIntensity h = new AnalogActionLightIntensity(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<VariableLight>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "variableLight");

        InstanceManager.getDefault(AnalogActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogActionLightIntensityXml.class);
}

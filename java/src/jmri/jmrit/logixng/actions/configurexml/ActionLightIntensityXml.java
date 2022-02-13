package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionLight;
import jmri.jmrit.logixng.actions.ActionLightIntensity;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;


/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionLightIntensityXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionLightIntensityXml() {
    }

    /**
     * Default implementation for storing the contents of a ActionLightIntensity
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionLightIntensity p = (ActionLightIntensity) o;

        Element element = new Element("ActionLightIntensity");
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

        Element e2 = new Element("IntensitySocket");
        e2.addContent(new Element("socketName").addContent(p.getChild(0).getName()));
        MaleSocket socket = p.getIntensitySocket().getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getIntensitySocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {

        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionLightIntensity h = new ActionLightIntensity(sys, uname);

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

        Element socketName = shared.getChild("IntensitySocket").getChild("socketName");
        h.getChild(0).setName(socketName.getTextTrim());
        Element socketSystemName = shared.getChild("IntensitySocket").getChild("systemName");
        if (socketSystemName != null) {
            h.setIntensitySystemName(socketSystemName.getTextTrim());
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLightIntensityXml.class);
}

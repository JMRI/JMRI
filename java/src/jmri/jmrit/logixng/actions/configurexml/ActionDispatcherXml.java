package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.actions.ActionDispatcher;
import jmri.jmrit.logixng.actions.ActionTurnout;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionDispatcher objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright (C) 2021
 */
public class ActionDispatcherXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionDispatcherXml() {
    }

    /**
     * Default implementation for storing the contents of a Dispatcher action
     *
     * @param o Object to store, of type ActionDispatcher
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionDispatcher p = (ActionDispatcher) o;

        Element element = new Element("ActionDispatcher");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectEnumXml = new LogixNG_SelectEnumXml<ActionDispatcher.DirectOperation>();

        String trainInfoFileName = p.getTrainInfoFileName();
        if (trainInfoFileName != null) {
            element.addContent(new Element("trainInfoFileName").addContent(trainInfoFileName));
        }

        element.addContent(new Element("addressing").addContent(p.getAddressing().name()));
        element.addContent(new Element("reference").addContent(p.getReference()));
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("formula").addContent(p.getFormula()));

        element.addContent(selectEnumXml.store(p.getSelectEnum(), "operation"));

        element.addContent(new Element("dataAddressing").addContent(p.getDataAddressing().name()));
        element.addContent(new Element("dataReference").addContent(p.getDataReference()));
        element.addContent(new Element("dataLocalVariable").addContent(p.getDataLocalVariable()));
        element.addContent(new Element("dataFormula").addContent(p.getDataFormula()));

        element.addContent(new Element("resetOption").addContent(p.getResetOption() ? "true" : "false"));
        element.addContent(new Element("terminateOption").addContent(p.getTerminateOption() ? "true" : "false"));
        element.addContent(new Element("trainPriority").addContent(String.valueOf(p.getTrainPriority())));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionDispatcher h = new ActionDispatcher(sys, uname);

        loadCommon(h, shared);

        var selectEnumXml = new LogixNG_SelectEnumXml<ActionDispatcher.DirectOperation>();

        selectEnumXml.load(shared.getChild("operation"), h.getSelectEnum());
        selectEnumXml.loadLegacy(
                shared, h.getSelectEnum(),
                "operationAddressing",
                "operationDirect",
                "operationReference",
                "operationLocalVariable",
                "operationFormula");

        try {
            Element elem = shared.getChild("trainInfoFileName");
            if (elem != null) {
                h.setTrainInfoFileName(elem.getTextTrim());
            }

            elem = shared.getChild("addressing");
            if (elem != null) {
                h.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild("reference");
            if (elem != null) h.setReference(elem.getTextTrim());

            elem = shared.getChild("localVariable");
            if (elem != null) h.setLocalVariable(elem.getTextTrim());

            elem = shared.getChild("formula");
            if (elem != null) h.setFormula(elem.getTextTrim());


            elem = shared.getChild("dataAddressing");
            if (elem != null) {
                h.setDataAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild("dataReference");
            if (elem != null) h.setDataReference(elem.getTextTrim());

            elem = shared.getChild("dataLocalVariable");
            if (elem != null) h.setDataLocalVariable(elem.getTextTrim());

            elem = shared.getChild("dataFormula");
            if (elem != null) h.setDataFormula(elem.getTextTrim());


            elem = shared.getChild("resetOption");
            h.setResetOption((elem != null) ? elem.getTextTrim().equals("true") : false);

            elem = shared.getChild("terminateOption");
            h.setTerminateOption((elem != null) ? elem.getTextTrim().equals("true") : false);

            elem = shared.getChild("trainPriority");
            if (elem != null) h.setTrainPriority(Integer.parseInt(elem.getTextTrim()));


        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionDispatcherXml.class);
}

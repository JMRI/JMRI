package jmri.jmrit.logixng.actions.configurexml;


import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.Memory;
import jmri.Reporter;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.actions.ActionReporter;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for Reporter objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright (C) 2021
 */
public class ActionReporterXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionReporterXml() {
    }

    /**
     * Default implementation for storing the contents of a Reporter
     *
     * @param o Object to store, of type Reporter
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionReporter p = (ActionReporter) o;

        Element element = new Element("ActionReporter");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Reporter>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        var selectMemoryNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();
        element.addContent(selectMemoryNamedBeanXml.store(p.getSelectMemoryNamedBean(), "memoryNamedBean"));

        element.addContent(new Element("reporterValue").addContent(p.getReporterValue().name()));

        element.addContent(new Element("dataAddressing").addContent(p.getDataAddressing().name()));
        element.addContent(new Element("dataReference").addContent(p.getDataReference()));
        element.addContent(new Element("dataLocalVariable").addContent(p.getDataLocalVariable()));
        element.addContent(new Element("dataFormula").addContent(p.getDataFormula()));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionReporter h = new ActionReporter(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Reporter>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "reporter");

        var selectMemoryNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();
        selectMemoryNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectMemoryNamedBean());
        selectMemoryNamedBeanXml.loadLegacy(shared, h.getSelectMemoryNamedBean(), "memory");

        try {
            Element elem = shared.getChild("reporterValue");
            if (elem != null) {
                h.setReporterValue(ActionReporter.ReporterValue.valueOf(elem.getTextTrim()));
            }

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

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionReporterXml.class);
}

package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ActionEntryExit;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import org.jdom2.Element;

/**
 * Handle XML configuration for EntryExit objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class ActionEntryExitXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionEntryExitXml() {
    }

    /**
     * Default implementation for storing the contents of a EntryExit
     *
     * @param o Object to store, of type TriggerEntryExit
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionEntryExit p = (ActionEntryExit) o;

        Element element = new Element("ActionEntryExit");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<DestinationPoints>();
        var selectEnumXml = new LogixNG_SelectEnumXml<ActionEntryExit.Operation>();

        element.addContent(selectNamedBeanXml.store(
                p.getSelectNamedBean(),
                "namedBean",
                LogixNG_SelectNamedBeanXml.StoreNamedBean.SystemName));
        element.addContent(selectEnumXml.store(p.getSelectEnum(), "operation"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionEntryExit h = new ActionEntryExit(sys, uname);

        loadCommon(h, shared);

        var selectEnumXml = new LogixNG_SelectEnumXml<ActionEntryExit.Operation>();

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<DestinationPoints>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean(), true);
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "destinationPoints");

        selectEnumXml.load(shared.getChild("operation"), h.getSelectEnum());
        selectEnumXml.loadLegacy(
                shared, h.getSelectEnum(),
                "operationAddressing",
                "operationDirect",
                "operationReference",
                "operationLocalVariable",
                "operationFormula");

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TriggerEntryExitXml.class);
}

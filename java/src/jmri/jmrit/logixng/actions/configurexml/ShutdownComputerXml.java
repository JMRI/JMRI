package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ShutdownComputer;

import org.jdom2.Element;

import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ShutdownComputerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ShutdownComputerXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleSensorSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ShutdownComputer p = (ShutdownComputer) o;

        Element element = new Element("ShutdownComputer");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectEnumXml = new LogixNG_SelectEnumXml<ShutdownComputer.Operation>();
        element.addContent(selectEnumXml.store(p.getSelectEnum(), "shutdownOperation"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);

        ShutdownComputer h = new ShutdownComputer(sys, uname);

        loadCommon(h, shared);

        var selectEnumXml = new LogixNG_SelectEnumXml<ShutdownComputer.Operation>();

        selectEnumXml.load(shared.getChild("shutdownOperation"), h.getSelectEnum());
        selectEnumXml.loadLegacy(
                shared, h.getSelectEnum(),
                null,
                "operation",
                null,
                null,
                null);

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShutdownComputerXml.class);
}

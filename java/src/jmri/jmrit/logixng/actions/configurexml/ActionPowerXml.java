package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ActionPower;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionPower objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class ActionPowerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionPowerXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TriplePowerSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionPower p = (ActionPower) o;

        Element element = new Element("ActionPower");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectEnumXml = new LogixNG_SelectEnumXml<ActionPower.PowerState>();
        element.addContent(selectEnumXml.store(p.getSelectEnum(), "state"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionPower h = new ActionPower(sys, uname);

        loadCommon(h, shared);

        var selectEnumXml = new LogixNG_SelectEnumXml<ActionPower.PowerState>();

        selectEnumXml.load(shared.getChild("state"), h.getSelectEnum());
        selectEnumXml.loadLegacy(
                shared, h.getSelectEnum(),
                null,
                "powerState",
                null,
                null,
                null);

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionPowerXml.class);
}

package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ActionRequestUpdateOfTurnout;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionRequestUpdateOfTurnout objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2025
 */
public class ActionRequestUpdateOfTurnoutXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionRequestUpdateOfTurnoutXml() {
    }

    /**
     * Default implementation for storing the contents of a ActionRequestUpdateOfTurnout
     *
     * @param o Object to store, of type ActionRequestUpdateOfTurnout
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionRequestUpdateOfTurnout p = (ActionRequestUpdateOfTurnout) o;

        Element element = new Element("ActionRequestUpdateOfTurnout");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Turnout>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionRequestUpdateOfTurnout h = new ActionRequestUpdateOfTurnout(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Turnout>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionRequestUpdateOfTurnoutXml.class);
}

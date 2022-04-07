package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.actions.ActionTurnoutLock;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionTurnoutLockXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionTurnoutLockXml() {
    }

    /**
     * Default implementation for storing the contents of a ActionTurnoutLock
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionTurnoutLock p = (ActionTurnoutLock) o;

        Element element = new Element("ActionTurnoutLock");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Turnout>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        element.addContent(new Element("stateAddressing").addContent(p.getLockAddressing().name()));
        element.addContent(new Element("turnoutLock").addContent(p.getTurnoutLock().name()));
        element.addContent(new Element("stateReference").addContent(p.getLockReference()));
        element.addContent(new Element("stateLocalVariable").addContent(p.getLockLocalVariable()));
        element.addContent(new Element("stateFormula").addContent(p.getLockFormula()));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionTurnoutLock h = new ActionTurnoutLock(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Turnout>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "turnout");

        try {
            Element elem = shared.getChild("stateAddressing");
            if (elem != null) {
                h.setLockAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            Element turnoutLock = shared.getChild("turnoutLock");
            if (turnoutLock != null) {
                h.setTurnoutLock(ActionTurnoutLock.TurnoutLock.valueOf(turnoutLock.getTextTrim()));
            }

            elem = shared.getChild("stateReference");
            if (elem != null) h.setLockReference(elem.getTextTrim());

            elem = shared.getChild("stateLocalVariable");
            if (elem != null) h.setLockLocalVariable(elem.getTextTrim());

            elem = shared.getChild("stateFormula");
            if (elem != null) h.setLockFormula(elem.getTextTrim());

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTurnoutLockXml.class);
}

package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.actions.ActionTurnoutLock;
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

        var turnout = p.getTurnout();
        if (turnout != null) {
            element.addContent(new Element("turnout").addContent(turnout.getName()));
        }

        element.addContent(new Element("addressing").addContent(p.getAddressing().name()));
        element.addContent(new Element("reference").addContent(p.getReference()));
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("formula").addContent(p.getFormula()));

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

        Element turnoutName = shared.getChild("turnout");
        if (turnoutName != null) {
            Turnout t = InstanceManager.getDefault(TurnoutManager.class).getTurnout(turnoutName.getTextTrim());
            if (t != null) h.setTurnout(t);
            else h.removeTurnout();
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


            elem = shared.getChild("stateAddressing");
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

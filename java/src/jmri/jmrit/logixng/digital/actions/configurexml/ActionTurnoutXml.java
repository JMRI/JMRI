package jmri.jmrit.logixng.digital.actions.configurexml;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.TurnoutManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.digital.actions.ActionTurnout;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionTurnoutXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionTurnoutXml() {
    }
    
    /**
     * Default implementation for storing the contents of a ActionTurnout
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionTurnout p = (ActionTurnout) o;

        Element element = new Element("action-turnout");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        NamedBeanHandle turnout = p.getTurnout();
        if (turnout != null) {
            element.addContent(new Element("turnout").addContent(turnout.getName()));
        }
        
        element.addContent(new Element("turnoutState").addContent(p.getTurnoutState().name()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionTurnout h = new ActionTurnout(sys, uname);

        loadCommon(h, shared);

        Element turnoutName = shared.getChild("turnout");
        if (turnoutName != null) {
            h.setTurnout(InstanceManager.getDefault(TurnoutManager.class).getTurnout(turnoutName.getTextTrim()));
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static Logger log = LoggerFactory.getLogger(ActionTurnoutXml.class);
}

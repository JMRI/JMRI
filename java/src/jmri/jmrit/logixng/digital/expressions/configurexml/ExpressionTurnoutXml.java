package jmri.jmrit.logixng.digital.expressions.configurexml;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.TurnoutManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.digital.expressions.ExpressionTurnout;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.logixng.Is_IsNot_Enum;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ExpressionTurnoutXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionTurnoutXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionTurnout p = (ExpressionTurnout) o;

        Element element = new Element("expression-turnout");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        NamedBeanHandle turnout = p.getTurnout();
        if (turnout != null) {
            element.addContent(new Element("turnout").addContent(turnout.getName()));
        }
        element.addContent(new Element("is_isNot").addContent(p.get_Is_IsNot().name()));
        element.addContent(new Element("turnoutState").addContent(p.getTurnoutState().name()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionTurnout h = new ExpressionTurnout(sys, uname);

        loadCommon(h, shared);

        Element turnoutName = shared.getChild("turnout");
        if (turnoutName != null) {
            h.setTurnout(InstanceManager.getDefault(TurnoutManager.class).getTurnout(turnoutName.getTextTrim()));
        }

        Element is_IsNot = shared.getChild("is_isNot");
        if (is_IsNot != null) {
            h.set_Is_IsNot(Is_IsNot_Enum.valueOf(is_IsNot.getTextTrim()));
        }

        Element turnoutState = shared.getChild("turnoutState");
        if (turnoutState != null) {
            h.setTurnoutState(ExpressionTurnout.TurnoutState.valueOf(turnoutState.getTextTrim()));
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
//    private final static Logger log = LoggerFactory.getLogger(ExpressionTurnoutXml.class);
}

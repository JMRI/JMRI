package jmri.jmrix.loconet.logixng.configurexml;

import java.util.*;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.logixng.ExpressionSlotUsage;

import org.jdom2.Element;

/**
 * Handle XML configuration for ExpressionSlotUsage objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public class ExpressionSlotUsageXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionSlotUsageXml() {
    }
    
    /**
     * Default implementation for storing the contents of a ExpressionSlotUsage
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionSlotUsage p = (ExpressionSlotUsage) o;

        Element element = new Element("ExpressionLoconetSlotUsage");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        if (p.getMemo() != null) {
            element.addContent(new Element("systemConnection")
                    .addContent(p.getMemo().getSystemPrefix()));
        }
        
        element.addContent(new Element("advanced").addContent(p.getAdvanced() ? "yes" : "no"));
        element.addContent(new Element("has_hasNot").addContent(p.get_Has_HasNot().name()));
        element.addContent(new Element("simpleState").addContent(p.getSimpleState().name()));
        
        Element advancedStatesElement = new Element("advancedStates");
        for (ExpressionSlotUsage.AdvancedState state : p.getAdvancedStates()) {
            advancedStatesElement.addContent(new Element("state").addContent(state.name()));
        }
        element.addContent(advancedStatesElement);
        element.addContent(new Element("compare").addContent(p.getCompare().name()));
        
        element.addContent(new Element("number").addContent(Integer.toString(p.getNumber())));
        
        element.addContent(new Element("percentPieces").addContent(p.getPercentPieces().name()));
        
        element.addContent(new Element("totalSlots").addContent(Integer.toString(p.getTotalSlots())));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionSlotUsage h = new ExpressionSlotUsage(sys, uname, null);

        loadCommon(h, shared);

        Element systemConnection = shared.getChild("systemConnection");
        if (systemConnection != null) {
            String systemConnectionName = systemConnection.getTextTrim();
            List<LocoNetSystemConnectionMemo> systemConnections =
                    jmri.InstanceManager.getList(LocoNetSystemConnectionMemo.class);
            
            for (LocoNetSystemConnectionMemo memo : systemConnections) {
                if (memo.getSystemPrefix().equals(systemConnectionName)) {
                    h.setMemo(memo);
                    break;
                }
            }
        }

        Element advanced = shared.getChild("advanced");
        if (advanced != null) {
            h.setAdvanced("yes".equals(advanced.getTextTrim()));
        } else {
            h.setAdvanced(false);
        }

        Element has_hasNot = shared.getChild("has_hasNot");
        if (has_hasNot != null) {
            h.set_Has_HasNot(ExpressionSlotUsage.Has_HasNot.valueOf(has_hasNot.getTextTrim()));
        }

        Element simpleState = shared.getChild("simpleState");
        if (simpleState != null) {
            h.setSimpleState(ExpressionSlotUsage.SimpleState.valueOf(simpleState.getTextTrim()));
        }

        Set<ExpressionSlotUsage.AdvancedState> stateSet = new HashSet<>();
        Element advancedStates = shared.getChild("advancedStates");
        for (Element state : advancedStates.getChildren()) {
            stateSet.add(ExpressionSlotUsage.AdvancedState.valueOf(state.getTextTrim()));
        }
        h.setAdvancedStates(stateSet);

        Element compare = shared.getChild("compare");
        if (compare != null) {
            h.setCompare(ExpressionSlotUsage.Compare.valueOf(compare.getTextTrim()));
        }

        Element number = shared.getChild("number");
        if (number != null) {
            h.setNumber(Integer.parseInt(number.getText()));
        }

        Element percentPieces = shared.getChild("percentPieces");
        if (percentPieces != null) {
            h.setPercentPieces(ExpressionSlotUsage.PercentPieces.valueOf(percentPieces.getTextTrim()));
        }

        Element totalNumber = shared.getChild("totalSlots");
        if (totalNumber != null) {
            h.setTotalSlots(Integer.parseInt(totalNumber.getText()));
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSlotUsageXml.class);
}

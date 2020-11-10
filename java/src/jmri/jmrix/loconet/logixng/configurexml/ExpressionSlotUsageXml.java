package jmri.jmrix.loconet.logixng.configurexml;

import java.util.List;

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

        Element element = new Element("expression-loconet-slot-usage");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        if (p.getMemo() != null) {
            element.addContent(new Element("systemConnection")
                    .addContent(p.getMemo().getSystemPrefix()));
        }
        
        StringBuilder slotStates = new StringBuilder();
        for (ExpressionSlotUsage.StateType state : p.getSlotStates()) {
            if (slotStates.length() > 0) slotStates.append(",");
            slotStates.append(state.name());
        }
        
        element.addContent(new Element("has_HasNot").addContent(p.getHasHasNot().name()));
        Element statesElement = new Element("states");
        for (ExpressionSlotUsage.StateType state : p.getSlotStates()) {
            element.addContent(new Element("state").addContent(state.name()));
        }
        element.addContent(statesElement);
        element.addContent(new Element("compare").addContent(p.getCompare().name()));
        element.addContent(new Element("percentPieces").addContent(p.getPercentPieces().name()));
        
        element.addContent(new Element("number").addContent(Integer.toString(p.getNumber())));

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
/*
        Element memoryName = shared.getChild("memory");
        if (memoryName != null) {
            Memory m = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
            if (m != null) h.setMemory(m);
            else h.removeMemory();
        }

        Element otherMemoryName = shared.getChild("otherMemory");
        if (otherMemoryName != null) {
            Memory m = InstanceManager.getDefault(MemoryManager.class).getMemory(otherMemoryName.getTextTrim());
            if (m != null) h.setOtherMemory(m);
            else h.removeOtherMemory();
        }

        Element constant = shared.getChild("constant");
        if (constant != null) {
            h.setConstantValue(constant.getText());
        }

        Element memoryOperation = shared.getChild("memoryOperation");
        if (memoryOperation != null) {
            h.setMemoryOperation(ExpressionSlotUsage.MemoryOperation.valueOf(memoryOperation.getTextTrim()));
        }

        Element compareTo = shared.getChild("compareTo");
        if (compareTo != null) {
            h.setCompareTo(ExpressionSlotUsage.CompareTo.valueOf(compareTo.getTextTrim()));
        }

        Element caseInsensitive = shared.getChild("caseInsensitive");
        if (caseInsensitive != null) {
            h.setCaseInsensitive("yes".equals(caseInsensitive.getTextTrim()));
        } else {
            h.setCaseInsensitive(false);
        }
*/
        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSlotUsageXml.class);
}

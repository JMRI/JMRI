package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ActionLocalVariable;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionLocalVariableXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionLocalVariableXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionLocalVariable p = (ActionLocalVariable) o;

        Element element = new Element("ActionLocalVariable");   // NOI18N
        element.setAttribute("class", this.getClass().getName());   // NOI18N
        element.addContent(new Element("systemName").addContent(p.getSystemName()));    // NOI18N

        storeCommon(p, element);

        String variableName = p.getLocalVariable();
        if (variableName != null) {
            element.addContent(new Element("variable").addContent(variableName));   // NOI18N
        }

        NamedBeanHandle<Memory> memoryName = p.getMemory();
        if (memoryName != null) {
            Element e = new Element("memory").addContent(memoryName.getName()); // NOI18N
            e.setAttribute("listen", p.getListenToMemory() ? "yes" : "no");  // NOI18N
            element.addContent(e);
        }

        NamedBeanHandle<Block> blockName = p.getBlock();
        if (blockName != null) {
            Element e = new Element("block").addContent(blockName.getName());   // NOI18N
            e.setAttribute("listen", p.getListenToBlock() ? "yes" : "no");  // NOI18N
            element.addContent(e);
        }

        NamedBeanHandle<Reporter> reporterName = p.getReporter();
        if (reporterName != null) {
            Element e = new Element("reporter").addContent(reporterName.getName()); // NOI18N
            e.setAttribute("listen", p.getListenToReporter() ? "yes" : "no");  // NOI18N
            element.addContent(e);
        }

        element.addContent(new Element("variableOperation").addContent(p.getVariableOperation().name()));   // NOI18N

        element.addContent(new Element("constant").addContent(p.getConstantValue()));   // NOI18N
        element.addContent(new Element("otherTableCell").addContent(p.getOtherTableCell()));    // NOI18N
        element.addContent(new Element("otherVariable").addContent(p.getOtherLocalVariable())); // NOI18N
        element.addContent(new Element("formula").addContent(p.getFormula()));  // NOI18N

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionLocalVariable h = new ActionLocalVariable(sys, uname);

        loadCommon(h, shared);

        Element variableName = shared.getChild("variable"); // NOI18N
        if (variableName != null) {
            h.setLocalVariable(variableName.getTextTrim());
        }

        Element memoryName = shared.getChild("memory"); // NOI18N
        if (memoryName != null) {
            Memory t = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
            if (t != null) h.setMemory(t);
            else h.removeMemory();

            String yesno = "yes";   // Default is "yes" since this attribute is not in panel files before 4.99.3
            if (memoryName.getAttribute("listen") != null) {  // NOI18N
                yesno = memoryName.getAttribute("listen").getValue();  // NOI18N
            }
            h.setListenToMemory(yesno.equals("yes"));   // NOI18N
        }

        Element blockName = shared.getChild("block");   // NOI18N
        if (blockName != null) {
            Block t = InstanceManager.getDefault(BlockManager.class).getBlock(blockName.getTextTrim());
            if (t != null) h.setBlock(t);
            else h.removeBlock();

            String yesno = "yes";   // Default is "yes" since this attribute is not in panel files before 4.99.3
            if (blockName.getAttribute("listen") != null) {  // NOI18N
                yesno = blockName.getAttribute("listen").getValue();  // NOI18N
            }
            h.setListenToBlock(yesno.equals("yes"));   // NOI18N
        }

        Element reporterName = shared.getChild("reporter"); // NOI18N
        if (reporterName != null) {
            Reporter t = InstanceManager.getDefault(ReporterManager.class).getReporter(reporterName.getTextTrim());
            if (t != null) h.setReporter(t);
            else h.removeReporter();

            String yesno = "yes";   // Default is "yes" since this attribute is not in panel files before 4.99.3
            if (reporterName.getAttribute("listen") != null) {  // NOI18N
                yesno = reporterName.getAttribute("listen").getValue();  // NOI18N
            }
            h.setListenToReporter(yesno.equals("yes"));   // NOI18N
        }

        Element queryType = shared.getChild("variableOperation");   // NOI18N
        if (queryType != null) {
            try {
                h.setVariableOperation(ActionLocalVariable.VariableOperation.valueOf(queryType.getTextTrim()));
            } catch (ParserException e) {
                log.error("cannot set variable operation: " + queryType.getTextTrim(), e);  // NOI18N
            }
        }

        Element constant = shared.getChild("constant"); // NOI18N
        if (constant != null) {
            h.setConstantValue(constant.getTextTrim());
        }

        Element otherTableCell = shared.getChild("otherTableCell"); // NOI18N
        if (otherTableCell != null) {
            h.setOtherTableCell(otherTableCell.getTextTrim());
        }

        Element otherVariable = shared.getChild("otherVariable");   // NOI18N
        if (otherVariable != null) {
            h.setOtherLocalVariable(otherVariable.getTextTrim());
        }

        Element formula = shared.getChild("formula");   // NOI18N
        if (formula != null) {
            try {
                h.setFormula(formula.getTextTrim());
            } catch (ParserException e) {
                log.error("cannot set data: " + formula.getTextTrim(), e);  // NOI18N
            }
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLocalVariableXml.class);
}

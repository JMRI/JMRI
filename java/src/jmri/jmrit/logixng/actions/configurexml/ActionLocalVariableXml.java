package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.jmrit.logixng.actions.ActionLocalVariable;
import jmri.jmrit.logixng.actions.ActionLocalVariable.ConstantType;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectTableXml;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLocalVariable objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionLocalVariableXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionLocalVariableXml() {
    }

    /**
     * Default implementation for storing the contents of a ActionLocalVariable
     *
     * @param o Object to store, of type ActionLocalVariable
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionLocalVariable p = (ActionLocalVariable) o;

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        Element element = new Element("ActionLocalVariable");   // NOI18N
        element.setAttribute("class", this.getClass().getName());   // NOI18N
        element.addContent(new Element("systemName").addContent(p.getSystemName()));    // NOI18N

        storeCommon(p, element);

        var selectMemoryNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();
        var selectBlockNamedBeanXml = new LogixNG_SelectNamedBeanXml<Block>();
        var selectReporterNamedBeanXml = new LogixNG_SelectNamedBeanXml<Reporter>();

        String variableName = p.getLocalVariable();
        if (variableName != null) {
            element.addContent(new Element("variable").addContent(variableName));   // NOI18N
        }

        element.addContent(selectMemoryNamedBeanXml.store(p.getSelectMemoryNamedBean(), "memoryNamedBean"));
        element.addContent(new Element("listenToMemory").addContent(p.getListenToMemory() ? "yes" : "no"));

        element.addContent(selectBlockNamedBeanXml.store(p.getSelectBlockNamedBean(), "blockNamedBean"));
        element.addContent(new Element("listenToBlock").addContent(p.getListenToBlock() ? "yes" : "no"));

        element.addContent(selectReporterNamedBeanXml.store(p.getSelectReporterNamedBean(), "reporterNamedBean"));
        element.addContent(new Element("listenToReporter").addContent(p.getListenToReporter() ? "yes" : "no"));

        element.addContent(new Element("variableOperation").addContent(p.getVariableOperation().name()));   // NOI18N

        element.addContent(new Element("constantType").addContent(p.getConstantType().name()));   // NOI18N
        element.addContent(new Element("constant").addContent(p.getConstantValue()));   // NOI18N
        element.addContent(new Element("otherVariable").addContent(p.getOtherLocalVariable())); // NOI18N
        element.addContent(new Element("reference").addContent(p.getReference())); // NOI18N
        element.addContent(new Element("formula").addContent(p.getFormula()));  // NOI18N

        element.addContent(selectTableXml.store(p.getSelectTable(), "table"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionLocalVariable h = new ActionLocalVariable(sys, uname);

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        loadCommon(h, shared);

        var selectMemoryNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();
        var selectBlockNamedBeanXml = new LogixNG_SelectNamedBeanXml<Block>();
        var selectReporterNamedBeanXml = new LogixNG_SelectNamedBeanXml<Reporter>();

        Element variableName = shared.getChild("variable"); // NOI18N
        if (variableName != null) {
            h.setLocalVariable(variableName.getTextTrim());
        }

        selectMemoryNamedBeanXml.load(shared.getChild("memoryNamedBean"), h.getSelectMemoryNamedBean());
        selectBlockNamedBeanXml.load(shared.getChild("blockNamedBean"), h.getSelectBlockNamedBean());
        selectReporterNamedBeanXml.load(shared.getChild("reporterNamedBean"), h.getSelectReporterNamedBean());

        Element listenToMemoryElem = shared.getChild("listenToMemory");
        if (listenToMemoryElem != null) {
            h.setListenToMemory("yes".equals(listenToMemoryElem.getTextTrim()));
        }

        Element listenToBlockElem = shared.getChild("listenToBlock");
        if (listenToBlockElem != null) {
            h.setListenToBlock("yes".equals(listenToBlockElem.getTextTrim()));
        }

        Element listenToReporterElem = shared.getChild("listenToReporter");
        if (listenToReporterElem != null) {
            h.setListenToReporter("yes".equals(listenToReporterElem.getTextTrim()));
        }

        //********************************************************
        // For backwards compability for 4.99.7 and earlier
        Element memoryName = shared.getChild("memory"); // NOI18N
        if (memoryName != null) {
            Memory t = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
            if (t != null) h.getSelectMemoryNamedBean().setNamedBean(t);
            else h.getSelectMemoryNamedBean().removeNamedBean();

            String yesno = "yes";   // Default is "yes" since this attribute is not in panel files before 4.99.3
            if (memoryName.getAttribute("listen") != null) {  // NOI18N
                yesno = memoryName.getAttribute("listen").getValue();  // NOI18N
            }
            h.setListenToMemory(yesno.equals("yes"));   // NOI18N
        }
        Element blockName = shared.getChild("block");   // NOI18N
        if (blockName != null) {
            Block t = InstanceManager.getDefault(BlockManager.class).getBlock(blockName.getTextTrim());
            if (t != null) h.getSelectBlockNamedBean().setNamedBean(t);
            else h.getSelectBlockNamedBean().removeNamedBean();

            String yesno = "yes";   // Default is "yes" since this attribute is not in panel files before 4.99.3
            if (blockName.getAttribute("listen") != null) {  // NOI18N
                yesno = blockName.getAttribute("listen").getValue();  // NOI18N
            }
            h.setListenToBlock(yesno.equals("yes"));   // NOI18N
        }
        Element reporterName = shared.getChild("reporter"); // NOI18N
        if (reporterName != null) {
            Reporter t = InstanceManager.getDefault(ReporterManager.class).getReporter(reporterName.getTextTrim());
            if (t != null) h.getSelectReporterNamedBean().setNamedBean(t);
            else h.getSelectReporterNamedBean().removeNamedBean();

            String yesno = "yes";   // Default is "yes" since this attribute is not in panel files before 4.99.3
            if (reporterName.getAttribute("listen") != null) {  // NOI18N
                yesno = reporterName.getAttribute("listen").getValue();  // NOI18N
            }
            h.setListenToReporter(yesno.equals("yes"));   // NOI18N
        }
        // For backwards compability for 4.99.7 and earlier
        //********************************************************

        Element queryType = shared.getChild("variableOperation");   // NOI18N
        if (queryType != null) {
            try {
                h.setVariableOperation(ActionLocalVariable.VariableOperation.valueOf(queryType.getTextTrim()));
            } catch (ParserException e) {
                log.error("cannot set variable operation: {}", queryType.getTextTrim(), e);  // NOI18N
            }
        }

        Element constantType = shared.getChild("constantType"); // NOI18N
        if (constantType != null) {
            h.setConstantType(ConstantType.valueOf(constantType.getTextTrim()));
        }

        Element constant = shared.getChild("constant"); // NOI18N
        if (constant != null) {
            h.setConstantValue(constant.getTextTrim());
        }

        Element otherTableCell = shared.getChild("otherTableCell"); // NOI18N
        if (otherTableCell != null) {
            boolean result = false;
            String ref = otherTableCell.getTextTrim();
            if (!ref.isEmpty()) {
                String[] refParts = ref.substring(1).split("[\\[\\]]");  // Remove first { and then split on [ and ]
//                System.out.format("refParts.length: %d, '%s', '%s'%n", refParts.length, refParts[0], refParts[1]);
                if (refParts.length == 3) {
                    String table = refParts[0];
                    String[] rowColumnParts = refParts[1].split(",");
                    if (rowColumnParts.length == 2) {
                        String row = rowColumnParts[0];
                        String column = rowColumnParts[1];
//                        System.out.format("Table: '%s', row: '%s', column: '%s'%n", table, row, column);

                        h.getSelectTable().setTableNameAddressing(NamedBeanAddressing.Direct);
                        if (table != null) {
                            NamedTable t = InstanceManager.getDefault(NamedTableManager.class).getNamedTable(table);
                            if (t != null) h.getSelectTable().setTable(t);
                            else h.getSelectTable().removeTable();
                        }
                        h.getSelectTable().setTableRowAddressing(NamedBeanAddressing.Direct);
                        h.getSelectTable().setTableRowName(row);
                        h.getSelectTable().setTableColumnAddressing(NamedBeanAddressing.Direct);
                        h.getSelectTable().setTableColumnName(column);
                        result = true;
                    }
                }
                if (!result) throw new JmriConfigureXmlException("otherTableCell has invalid value: "+ref);
            }
        }

        selectTableXml.load(shared.getChild("table"), h.getSelectTable());

        Element otherVariable = shared.getChild("otherVariable");   // NOI18N
        if (otherVariable != null) {
            h.setOtherLocalVariable(otherVariable.getTextTrim());
        }

        Element reference = shared.getChild("reference");   // NOI18N
        if (reference != null) {
            h.setReference(reference.getTextTrim());
        }

        Element formula = shared.getChild("formula");   // NOI18N
        if (formula != null) {
            try {
                h.setFormula(formula.getTextTrim());
            } catch (ParserException e) {
                log.error("cannot set data: {}", formula.getTextTrim(), e);  // NOI18N
            }
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLocalVariableXml.class);
}

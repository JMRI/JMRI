package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.jmrit.logixng.actions.ActionTable;
import jmri.jmrit.logixng.actions.ActionTable.ConstantType;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectTableXml;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionTable objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionTableXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionTableXml() {
    }

    /**
     * Default implementation for storing the contents of a ActionTable
     *
     * @param o Object to store, of type ActionTable
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionTable p = (ActionTable) o;

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        Element element = new Element("ActionTable");   // NOI18N
        element.setAttribute("class", this.getClass().getName());   // NOI18N
        element.addContent(new Element("systemName").addContent(p.getSystemName()));    // NOI18N

        storeCommon(p, element);

        var selectMemoryNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();
        var selectBlockNamedBeanXml = new LogixNG_SelectNamedBeanXml<Block>();
        var selectReporterNamedBeanXml = new LogixNG_SelectNamedBeanXml<Reporter>();

        element.addContent(selectTableXml.store(p.getSelectTableToSet(), "tableToSet"));

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
        ActionTable h = new ActionTable(sys, uname);

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        loadCommon(h, shared);

        var selectMemoryNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();
        var selectBlockNamedBeanXml = new LogixNG_SelectNamedBeanXml<Block>();
        var selectReporterNamedBeanXml = new LogixNG_SelectNamedBeanXml<Reporter>();

        selectTableXml.load(shared.getChild("tableToSet"), h.getSelectTableToSet());

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

        Element queryType = shared.getChild("variableOperation");   // NOI18N
        if (queryType != null) {
            try {
                h.setVariableOperation(ActionTable.VariableOperation.valueOf(queryType.getTextTrim()));
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTableXml.class);
}

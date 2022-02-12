package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.NamedTableManager;
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
        element.addContent(new Element("otherVariable").addContent(p.getOtherLocalVariable())); // NOI18N
        element.addContent(new Element("formula").addContent(p.getFormula()));  // NOI18N


        Element tableElement = new Element("table");
        element.addContent(tableElement);

        Element tableNameElement = new Element("tableName");
        tableNameElement.addContent(new Element("addressing").addContent(p.getTableNameAddressing().name()));
        var table = p.getTable();
        if (table != null) {
            tableNameElement.addContent(new Element("name").addContent(table.getName()));
        }
        tableNameElement.addContent(new Element("reference").addContent(p.getTableNameReference()));
        tableNameElement.addContent(new Element("localVariable").addContent(p.getTableNameLocalVariable()));
        tableNameElement.addContent(new Element("formula").addContent(p.getTableNameFormula()));
        tableElement.addContent(tableNameElement);

        Element tableRowElement = new Element("row");
        tableRowElement.addContent(new Element("addressing").addContent(p.getTableRowAddressing().name()));
        tableRowElement.addContent(new Element("name").addContent(p.getTableRowName()));
        tableRowElement.addContent(new Element("reference").addContent(p.getTableRowReference()));
        tableRowElement.addContent(new Element("localVariable").addContent(p.getTableRowLocalVariable()));
        tableRowElement.addContent(new Element("formula").addContent(p.getTableRowFormula()));
        tableElement.addContent(tableRowElement);

        Element tableColumnElement = new Element("column");
        tableColumnElement.addContent(new Element("addressing").addContent(p.getTableColumnAddressing().name()));
        tableColumnElement.addContent(new Element("name").addContent(p.getTableColumnName()));
        tableColumnElement.addContent(new Element("reference").addContent(p.getTableColumnReference()));
        tableColumnElement.addContent(new Element("localVariable").addContent(p.getTableColumnLocalVariable()));
        tableColumnElement.addContent(new Element("formula").addContent(p.getTableColumnFormula()));
        tableElement.addContent(tableColumnElement);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
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
            boolean result = false;
            String ref = otherTableCell.getTextTrim();
            String[] refParts = ref.substring(1).split("[\\[\\]]");  // Remove first { and then split on [ and ]
//            System.out.format("refParts.length: %d, '%s', '%s'%n", refParts.length, refParts[0], refParts[1]);
            if (refParts.length == 3) {
                String table = refParts[0];
                String[] rowColumnParts = refParts[1].split(",");
                if (rowColumnParts.length == 2) {
                    String row = rowColumnParts[0];
                    String column = rowColumnParts[1];
//                    System.out.format("Table: '%s', row: '%s', column: '%s'%n", table, row, column);

                    h.setTableNameAddressing(NamedBeanAddressing.Direct);
                    if (table != null) {
                        NamedTable t = InstanceManager.getDefault(NamedTableManager.class).getNamedTable(table);
                        if (t != null) h.setTable(t);
                        else h.removeTable();
                    }
                    h.setTableRowAddressing(NamedBeanAddressing.Direct);
                    h.setTableRowName(row);
                    h.setTableColumnAddressing(NamedBeanAddressing.Direct);
                    h.setTableColumnName(column);
                    result = true;
                }
            }
            if (!result) throw new JmriConfigureXmlException("otherTableCell has invalid value: "+ref);
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


        Element tableElement = shared.getChild("table");

        if (tableElement != null) {
            try {
                Element tableName = tableElement.getChild("tableName");
                Element name = tableName.getChild("name");
                if (name != null) {
                    NamedTable t = InstanceManager.getDefault(NamedTableManager.class).getNamedTable(name.getTextTrim());
                    if (t != null) h.setTable(t);
                    else h.removeTable();
                }

                Element elem = tableName.getChild("addressing");
                if (elem != null) {
                    h.setTableNameAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                elem = tableName.getChild("reference");
                if (elem != null) h.setTableNameReference(elem.getTextTrim());

                elem = tableName.getChild("localVariable");
                if (elem != null) h.setTableNameLocalVariable(elem.getTextTrim());

                elem = tableName.getChild("formula");
                if (elem != null) h.setTableNameFormula(elem.getTextTrim());


                Element tableRow = tableElement.getChild("row");
                elem = tableRow.getChild("addressing");
                if (elem != null) {
                    h.setTableRowAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                name = tableRow.getChild("name");
                if (name != null) {
                    h.setTableRowName(name.getTextTrim());
                }

                elem = tableRow.getChild("reference");
                if (elem != null) h.setTableRowReference(elem.getTextTrim());

                elem = tableRow.getChild("localVariable");
                if (elem != null) h.setTableRowLocalVariable(elem.getTextTrim());

                elem = tableRow.getChild("formula");
                if (elem != null) h.setTableRowFormula(elem.getTextTrim());


                Element tableColumn = tableElement.getChild("column");
                elem = tableColumn.getChild("addressing");
                if (elem != null) {
                    h.setTableColumnAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                name = tableColumn.getChild("name");
                if (name != null) {
                    h.setTableColumnName(name.getTextTrim());
                }

                elem = tableColumn.getChild("reference");
                if (elem != null) h.setTableColumnReference(elem.getTextTrim());

                elem = tableColumn.getChild("localVariable");
                if (elem != null) h.setTableColumnLocalVariable(elem.getTextTrim());

                elem = tableColumn.getChild("formula");
                if (elem != null) h.setTableColumnFormula(elem.getTextTrim());

            } catch (ParserException e) {
                throw new JmriConfigureXmlException(e);
            }
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLocalVariableXml.class);
}

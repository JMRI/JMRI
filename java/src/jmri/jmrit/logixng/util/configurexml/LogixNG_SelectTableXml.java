package jmri.jmrit.logixng.util.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.jmrit.logixng.util.LogixNG_SelectTable;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Xml class for jmri.jmrit.logixng.util.LogixNG_SelectTable.
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectTableXml {

    /**
     * Default implementation for storing the contents of a LogixNG_SelectTable
     *
     * @param selectTable the LogixNG_SelectTable object
     * @param tagName the name of the element
     * @return Element containing the complete info
     */
    public Element store(LogixNG_SelectTable selectTable, String tagName) {
        Element tableElement = new Element(tagName);

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        Element tableNameElement = new Element("tableName");
        tableNameElement.addContent(new Element("addressing").addContent(selectTable.getTableNameAddressing().name()));
        NamedBeanHandle<NamedTable> table = selectTable.getTable();
        if (table != null) {
            tableNameElement.addContent(new Element("name").addContent(table.getName()));
        }
        if (selectTable.getTableNameReference() != null && !selectTable.getTableNameReference().isEmpty()) {
            tableNameElement.addContent(new Element("reference").addContent(selectTable.getTableNameReference()));
        }
        var memory = selectTable.getTableNameMemory();
        if (memory != null) {
            tableNameElement.addContent(new Element("memory").addContent(memory.getName()));
        }
        if (selectTable.getTableNameLocalVariable() != null && !selectTable.getTableNameLocalVariable().isEmpty()) {
            tableNameElement.addContent(new Element("localVariable").addContent(selectTable.getTableNameLocalVariable()));
        }
        if (selectTable.getTableNameFormula() != null && !selectTable.getTableNameFormula().isEmpty()) {
            tableNameElement.addContent(new Element("formula").addContent(selectTable.getTableNameFormula()));
        }
        if (selectTable.getTableNameAddressing() == NamedBeanAddressing.Table) {
            tableNameElement.addContent(selectTableXml.store(selectTable.getSelectTableName(), "table"));
        }
        tableElement.addContent(tableNameElement);

        Element tableRowElement = new Element("row");
        tableRowElement.addContent(new Element("addressing").addContent(selectTable.getTableRowAddressing().name()));
        if (selectTable.getTableRowName() != null && !selectTable.getTableRowName().isEmpty()) {
            tableRowElement.addContent(new Element("name").addContent(selectTable.getTableRowName()));
        }
        if (selectTable.getTableRowReference() != null && !selectTable.getTableRowReference().isEmpty()) {
            tableRowElement.addContent(new Element("reference").addContent(selectTable.getTableRowReference()));
        }
        memory = selectTable.getTableRowMemory();
        if (memory != null) {
            tableRowElement.addContent(new Element("memory").addContent(memory.getName()));
        }
        if (selectTable.getTableRowLocalVariable() != null && !selectTable.getTableRowLocalVariable().isEmpty()) {
            tableRowElement.addContent(new Element("localVariable").addContent(selectTable.getTableRowLocalVariable()));
        }
        if (selectTable.getTableRowFormula() != null && !selectTable.getTableRowFormula().isEmpty()) {
            tableRowElement.addContent(new Element("formula").addContent(selectTable.getTableRowFormula()));
        }
        if (selectTable.getTableRowAddressing() == NamedBeanAddressing.Table) {
            tableRowElement.addContent(selectTableXml.store(selectTable.getSelectTableRow(), "table"));
        }
        tableElement.addContent(tableRowElement);

        Element tableColumnElement = new Element("column");
        tableColumnElement.addContent(new Element("addressing").addContent(selectTable.getTableColumnAddressing().name()));
        if (selectTable.getTableColumnName() != null && !selectTable.getTableColumnName().isEmpty()) {
            tableColumnElement.addContent(new Element("name").addContent(selectTable.getTableColumnName()));
        }
        if (selectTable.getTableColumnReference() != null && !selectTable.getTableColumnReference().isEmpty()) {
            tableColumnElement.addContent(new Element("reference").addContent(selectTable.getTableColumnReference()));
        }
        memory = selectTable.getTableColumnMemory();
        if (memory != null) {
            tableColumnElement.addContent(new Element("memory").addContent(memory.getName()));
        }
        if (selectTable.getTableColumnLocalVariable() != null && !selectTable.getTableColumnLocalVariable().isEmpty()) {
            tableColumnElement.addContent(new Element("localVariable").addContent(selectTable.getTableColumnLocalVariable()));
        }
        if (selectTable.getTableColumnFormula() != null && !selectTable.getTableColumnFormula().isEmpty()) {
            tableColumnElement.addContent(new Element("formula").addContent(selectTable.getTableColumnFormula()));
        }
        if (selectTable.getTableColumnAddressing() == NamedBeanAddressing.Table) {
            tableColumnElement.addContent(selectTableXml.store(selectTable.getSelectTableColumn(), "table"));
        }
        tableElement.addContent(tableColumnElement);

        return tableElement;
    }

    public void load(Element tableElement, LogixNG_SelectTable selectTable) throws JmriConfigureXmlException {

        if (tableElement != null) {

            LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

            try {
                Element tableName = tableElement.getChild("tableName");

                // Table name

                Element elem = tableName.getChild("addressing");
                if (elem != null) {
                    selectTable.setTableNameAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                elem = tableName.getChild("name");
                if (elem != null) {
                    NamedTable t = InstanceManager.getDefault(NamedTableManager.class).getNamedTable(elem.getTextTrim());
                    if (t != null) selectTable.setTable(t);
                    else selectTable.removeTable();
                }

                elem = tableName.getChild("reference");
                if (elem != null) selectTable.setTableNameReference(elem.getTextTrim());

                Element memoryName = tableName.getChild("memory");
                if (memoryName != null) {
                    Memory m = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
                    if (m != null) selectTable.setTableNameMemory(m);
                    else selectTable.removeTableNameMemory();
                }

                elem = tableName.getChild("localVariable");
                if (elem != null) selectTable.setTableNameLocalVariable(elem.getTextTrim());

                elem = tableName.getChild("formula");
                if (elem != null) selectTable.setTableNameFormula(elem.getTextTrim());

                elem = tableName.getChild("table");
                if (elem != null) {
                    selectTableXml.load(elem, selectTable.getSelectTableName());
                }


                // Table row

                Element tableRow = tableElement.getChild("row");
                elem = tableRow.getChild("addressing");
                if (elem != null) {
                    selectTable.setTableRowAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                elem = tableRow.getChild("name");
                if (elem != null) {
                    selectTable.setTableRowName(elem.getTextTrim());
                }

                elem = tableRow.getChild("reference");
                if (elem != null) selectTable.setTableRowReference(elem.getTextTrim());

                memoryName = tableRow.getChild("memory");
                if (memoryName == null
                        && selectTable.getTableNameAddressing() != NamedBeanAddressing.Memory
                        && selectTable.getTableRowAddressing() == NamedBeanAddressing.Memory
                        && selectTable.getTableColumnAddressing() != NamedBeanAddressing.Memory) {
                    // Handle bug pre JMRI 5.7.6
                    memoryName = tableName.getChild("memory");
                    selectTable.removeTableNameMemory();
                }
                if (memoryName != null) {
                    Memory m = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
                    if (m != null) selectTable.setTableRowMemory(m);
                    else selectTable.removeTableRowMemory();
                }

                elem = tableRow.getChild("localVariable");
                if (elem != null) selectTable.setTableRowLocalVariable(elem.getTextTrim());

                elem = tableRow.getChild("formula");
                if (elem != null) selectTable.setTableRowFormula(elem.getTextTrim());

                elem = tableRow.getChild("table");
                if (elem != null) {
                    selectTableXml.load(elem, selectTable.getSelectTableRow());
                }


                // Table column

                Element tableColumn = tableElement.getChild("column");
                elem = tableColumn.getChild("addressing");
                if (elem != null) {
                    selectTable.setTableColumnAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                elem = tableColumn.getChild("name");
                if (elem != null) {
                    selectTable.setTableColumnName(elem.getTextTrim());
                }

                elem = tableColumn.getChild("reference");
                if (elem != null) selectTable.setTableColumnReference(elem.getTextTrim());

                memoryName = tableColumn.getChild("memory");
                if (memoryName == null
                        && selectTable.getTableNameAddressing() != NamedBeanAddressing.Memory
                        && selectTable.getTableRowAddressing() != NamedBeanAddressing.Memory
                        && selectTable.getTableColumnAddressing() == NamedBeanAddressing.Memory) {
                    // Handle bug pre JMRI 5.7.6
                    memoryName = tableName.getChild("memory");
                    selectTable.removeTableNameMemory();
                }
                if (memoryName != null) {
                    Memory m = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
                    if (m != null) selectTable.setTableColumnMemory(m);
                    else selectTable.removeTableColumnMemory();
                }

                elem = tableColumn.getChild("localVariable");
                if (elem != null) selectTable.setTableColumnLocalVariable(elem.getTextTrim());

                elem = tableColumn.getChild("formula");
                if (elem != null) selectTable.setTableColumnFormula(elem.getTextTrim());

                elem = tableColumn.getChild("table");
                if (elem != null) {
                    selectTableXml.load(elem, selectTable.getSelectTableColumn());
                }

            } catch (ParserException e) {
                throw new JmriConfigureXmlException(e);
            }
        }
    }

}

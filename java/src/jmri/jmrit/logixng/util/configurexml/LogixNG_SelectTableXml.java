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
     * @return Element containing the complete info
     */
    public Element store(LogixNG_SelectTable selectTable) {
        Element tableElement = new Element("table");

        Element tableNameElement = new Element("tableName");
        tableNameElement.addContent(new Element("addressing").addContent(selectTable.getTableNameAddressing().name()));
        NamedBeanHandle<NamedTable> table = selectTable.getTable();
        if (table != null) {
            tableNameElement.addContent(new Element("name").addContent(table.getName()));
        }
        tableNameElement.addContent(new Element("reference").addContent(selectTable.getTableNameReference()));
        tableNameElement.addContent(new Element("localVariable").addContent(selectTable.getTableNameLocalVariable()));
        tableNameElement.addContent(new Element("formula").addContent(selectTable.getTableNameFormula()));
        tableElement.addContent(tableNameElement);

        Element tableRowElement = new Element("row");
        tableRowElement.addContent(new Element("addressing").addContent(selectTable.getTableRowAddressing().name()));
        tableRowElement.addContent(new Element("name").addContent(selectTable.getTableRowName()));
        tableRowElement.addContent(new Element("reference").addContent(selectTable.getTableRowReference()));
        tableRowElement.addContent(new Element("localVariable").addContent(selectTable.getTableRowLocalVariable()));
        tableRowElement.addContent(new Element("formula").addContent(selectTable.getTableRowFormula()));
        tableElement.addContent(tableRowElement);

        Element tableColumnElement = new Element("column");
        tableColumnElement.addContent(new Element("addressing").addContent(selectTable.getTableColumnAddressing().name()));
        tableColumnElement.addContent(new Element("name").addContent(selectTable.getTableColumnName()));
        tableColumnElement.addContent(new Element("reference").addContent(selectTable.getTableColumnReference()));
        tableColumnElement.addContent(new Element("localVariable").addContent(selectTable.getTableColumnLocalVariable()));
        tableColumnElement.addContent(new Element("formula").addContent(selectTable.getTableColumnFormula()));
        tableElement.addContent(tableColumnElement);

        return tableElement;
    }

    public void load(Element tableElement, LogixNG_SelectTable selectTable) throws JmriConfigureXmlException {

        if (tableElement != null) {
            try {
                Element tableName = tableElement.getChild("tableName");
                Element name = tableName.getChild("name");
                if (name != null) {
                    NamedTable t = InstanceManager.getDefault(NamedTableManager.class).getNamedTable(name.getTextTrim());
                    if (t != null) selectTable.setTable(t);
                    else selectTable.removeTable();
                }

                Element elem = tableName.getChild("addressing");
                if (elem != null) {
                    selectTable.setTableNameAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                elem = tableName.getChild("reference");
                if (elem != null) selectTable.setTableNameReference(elem.getTextTrim());

                elem = tableName.getChild("localVariable");
                if (elem != null) selectTable.setTableNameLocalVariable(elem.getTextTrim());

                elem = tableName.getChild("formula");
                if (elem != null) selectTable.setTableNameFormula(elem.getTextTrim());


                Element tableRow = tableElement.getChild("row");
                elem = tableRow.getChild("addressing");
                if (elem != null) {
                    selectTable.setTableRowAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                name = tableRow.getChild("name");
                if (name != null) {
                    selectTable.setTableRowName(name.getTextTrim());
                }

                elem = tableRow.getChild("reference");
                if (elem != null) selectTable.setTableRowReference(elem.getTextTrim());

                elem = tableRow.getChild("localVariable");
                if (elem != null) selectTable.setTableRowLocalVariable(elem.getTextTrim());

                elem = tableRow.getChild("formula");
                if (elem != null) selectTable.setTableRowFormula(elem.getTextTrim());


                Element tableColumn = tableElement.getChild("column");
                elem = tableColumn.getChild("addressing");
                if (elem != null) {
                    selectTable.setTableColumnAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                name = tableColumn.getChild("name");
                if (name != null) {
                    selectTable.setTableColumnName(name.getTextTrim());
                }

                elem = tableColumn.getChild("reference");
                if (elem != null) selectTable.setTableColumnReference(elem.getTextTrim());

                elem = tableColumn.getChild("localVariable");
                if (elem != null) selectTable.setTableColumnLocalVariable(elem.getTextTrim());

                elem = tableColumn.getChild("formula");
                if (elem != null) selectTable.setTableColumnFormula(elem.getTextTrim());

            } catch (ParserException e) {
                throw new JmriConfigureXmlException(e);
            }
        }
    }

}

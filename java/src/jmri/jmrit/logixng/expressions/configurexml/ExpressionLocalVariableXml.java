package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.jmrit.logixng.expressions.ExpressionLocalVariable;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ExpressionLocalVariableXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionLocalVariableXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionLocalVariable p = (ExpressionLocalVariable) o;

        Element element = new Element("ExpressionLocalVariable");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        String variableName = p.getLocalVariable();
        if (variableName != null) {
            element.addContent(new Element("variable").addContent(variableName));
        }
        String otherVariableName = p.getOtherLocalVariable();
        if (otherVariableName != null) {
            element.addContent(new Element("otherVariable").addContent(otherVariableName));
        }

        NamedBeanHandle<Memory> memoryName = p.getMemory();
        if (memoryName != null) {
            element.addContent(new Element("memory").addContent(memoryName.getName()));
        }

        element.addContent(new Element("compareTo").addContent(p.getCompareTo().name()));
        element.addContent(new Element("variableOperation").addContent(p.getVariableOperation().name()));
        element.addContent(new Element("caseInsensitive").addContent(p.getCaseInsensitive() ? "yes" : "no"));

        element.addContent(new Element("constant").addContent(p.getConstantValue()));
        element.addContent(new Element("regEx").addContent(p.getRegEx()));


        Element tableElement = new Element("table");
        element.addContent(tableElement);

        Element tableNameElement = new Element("tableName");
        tableNameElement.addContent(new Element("addressing").addContent(p.getTableNameAddressing().name()));
        NamedBeanHandle table = p.getTable();
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
        ExpressionLocalVariable h = new ExpressionLocalVariable(sys, uname);

        loadCommon(h, shared);

        Element variableName = shared.getChild("variable");
        if (variableName != null) {
            h.setLocalVariable(variableName.getTextTrim());
        }

        Element otherVariableName = shared.getChild("otherVariable");
        if (otherVariableName != null) {
            h.setOtherLocalVariable(otherVariableName.getTextTrim());
        }

        Element memoryName = shared.getChild("memory");
        if (memoryName != null) {
            Memory t = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
            if (t != null) h.setMemory(t);
            else h.removeMemory();
        }

        Element constant = shared.getChild("constant");
        if (constant != null) {
            h.setConstantValue(constant.getText());
        }

        Element regEx = shared.getChild("regEx");
        if (regEx != null) {
            h.setRegEx(regEx.getText());
        }

        Element compareTo = shared.getChild("compareTo");
        if (compareTo != null) {
            h.setCompareTo(ExpressionLocalVariable.CompareTo.valueOf(compareTo.getTextTrim()));
        }

        Element variableOperation = shared.getChild("variableOperation");
        if (variableOperation != null) {
            h.setVariableOperation(ExpressionLocalVariable.VariableOperation.valueOf(variableOperation.getTextTrim()));
        }

        Element caseInsensitive = shared.getChild("caseInsensitive");
        if (caseInsensitive != null) {
            h.setCaseInsensitive("yes".equals(caseInsensitive.getTextTrim()));
        } else {
            h.setCaseInsensitive(false);
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


        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionLocalVariableXml.class);
}

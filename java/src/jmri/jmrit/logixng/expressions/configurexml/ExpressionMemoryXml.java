package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.jmrit.logixng.expressions.ExpressionMemory;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ExpressionMemoryXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionMemoryXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionMemory p = (ExpressionMemory) o;

        Element element = new Element("ExpressionMemory");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        NamedBeanHandle memory = p.getMemory();
        if (memory != null) {
            element.addContent(new Element("memory").addContent(memory.getName()));
        }
        NamedBeanHandle otherMemory = p.getOtherMemory();
        if (otherMemory != null) {
            element.addContent(new Element("otherMemory").addContent(otherMemory.getName()));
        }
        NamedBeanHandle table = p.getTable();
        if (table != null) {
            element.addContent(new Element("table").addContent(table.getName()));
        }
        element.addContent(new Element("tableRow").addContent(p.getTableRowName()));
        element.addContent(new Element("tableColumn").addContent(p.getTableColumnName()));
        
        String variableName = p.getLocalVariable();
        if (variableName != null) {
            element.addContent(new Element("variable").addContent(variableName));
        }
        
        element.addContent(new Element("compareTo").addContent(p.getCompareTo().name()));
        element.addContent(new Element("memoryOperation").addContent(p.getMemoryOperation().name()));
        element.addContent(new Element("caseInsensitive").addContent(p.getCaseInsensitive() ? "yes" : "no"));
        
        element.addContent(new Element("constant").addContent(p.getConstantValue()));
        element.addContent(new Element("regEx").addContent(p.getRegEx()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionMemory h = new ExpressionMemory(sys, uname);

        loadCommon(h, shared);

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

        Element tableName = shared.getChild("table");
        if (tableName != null) {
            NamedTable t = InstanceManager.getDefault(NamedTableManager.class).getNamedTable(tableName.getTextTrim());
            if (t != null) h.setTable(t);
            else h.removeTable();
        }

        Element tableRowName = shared.getChild("tableRow");
        if (tableRowName != null) {
            h.setTableRowName(tableRowName.getTextTrim());
        }

        Element tableColumnName = shared.getChild("tableColumn");
        if (tableColumnName != null) {
            h.setTableColumnName(tableColumnName.getTextTrim());
        }

        Element variableName = shared.getChild("variable");
        if (variableName != null) {
            h.setLocalVariable(variableName.getTextTrim());
        }

        Element constant = shared.getChild("constant");
        if (constant != null) {
            h.setConstantValue(constant.getText());
        }

        Element regEx = shared.getChild("regEx");
        if (regEx != null) {
            h.setRegEx(regEx.getText());
        }

        Element memoryOperation = shared.getChild("memoryOperation");
        if (memoryOperation != null) {
            h.setMemoryOperation(ExpressionMemory.MemoryOperation.valueOf(memoryOperation.getTextTrim()));
        }

        Element compareTo = shared.getChild("compareTo");
        if (compareTo != null) {
            h.setCompareTo(ExpressionMemory.CompareTo.valueOf(compareTo.getTextTrim()));
        }

        Element caseInsensitive = shared.getChild("caseInsensitive");
        if (caseInsensitive != null) {
            h.setCaseInsensitive("yes".equals(caseInsensitive.getTextTrim()));
        } else {
            h.setCaseInsensitive(false);
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionTurnoutXml.class);
}

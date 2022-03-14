package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBeanHandle;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.jmrit.logixng.actions.ActionMemory;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectTableXml;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionMemory objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionMemoryXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionMemoryXml() {
    }

    /**
     * Default implementation for storing the contents of a ActionMemory
     *
     * @param o Object to store, of type ActionMemory
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionMemory p = (ActionMemory) o;

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        Element element = new Element("ActionMemory");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        NamedBeanHandle<Memory> memory = p.getMemory();
        if (memory != null) {
            element.addContent(new Element("memory").addContent(memory.getName()));
        }

        element.addContent(new Element("addressing").addContent(p.getAddressing().name()));
        element.addContent(new Element("reference").addContent(p.getReference()));
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("formula").addContent(p.getFormula()));

        NamedBeanHandle<Memory> otherMemoryName = p.getOtherMemory();
        if (otherMemoryName != null) {
            element.addContent(new Element("otherMemory").addContent(otherMemoryName.getName()));
        }

        element.addContent(new Element("memoryOperation").addContent(p.getMemoryOperation().name()));

        element.addContent(new Element("otherConstant").addContent(p.getConstantValue()));
        element.addContent(new Element("otherVariable").addContent(p.getOtherLocalVariable()));
        element.addContent(new Element("otherFormula").addContent(p.getOtherFormula()));

        element.addContent(selectTableXml.store(p.getSelectTable(), "table"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionMemory h = new ActionMemory(sys, uname);

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        loadCommon(h, shared);


        Element memoryName = shared.getChild("memory");
        if (memoryName != null) {
            Memory t = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
            if (t != null) h.setMemory(t);
            else h.removeMemory();
        }

        Element otherMemoryName = shared.getChild("otherMemory");
        if (otherMemoryName != null) {
            Memory t = InstanceManager.getDefault(MemoryManager.class).getMemory(otherMemoryName.getTextTrim());
            if (t != null) h.setOtherMemory(t);
            else h.removeOtherMemory();
        }

        Element queryType = shared.getChild("memoryOperation");
        if (queryType != null) {
            try {
                h.setMemoryOperation(ActionMemory.MemoryOperation.valueOf(queryType.getTextTrim()));
            } catch (ParserException e) {
                log.error("cannot set memory operation: " + queryType.getTextTrim(), e);
            }
        }


        Element elemAddressing = shared.getChild("addressing");

        // Temporary solution for handling change in the xml file.
        // Remove this when JMRI 4.24 is released.
        if (elemAddressing == null) {
            Element constant = shared.getChild("constant");
            if (constant != null) {
                h.setOtherConstantValue(constant.getTextTrim());
            }

            Element variable = shared.getChild("variable");
            if (variable != null) {
                h.setOtherLocalVariable(variable.getTextTrim());
            }

            Element formula = shared.getChild("formula");
            if (formula != null) {
                try {
                    h.setOtherFormula(formula.getTextTrim());
                } catch (ParserException e) {
                    log.error("cannot set data: " + formula.getTextTrim(), e);
                }
            }
        } else {
            try {
                h.setAddressing(NamedBeanAddressing.valueOf(elemAddressing.getTextTrim()));

                Element elem = shared.getChild("reference");
                if (elem != null) h.setReference(elem.getTextTrim());

                elem = shared.getChild("localVariable");
                if (elem != null) h.setLocalVariable(elem.getTextTrim());

                elem = shared.getChild("formula");
                if (elem != null) h.setFormula(elem.getTextTrim());

                elem = shared.getChild("otherConstant");
                if (elem != null) h.setOtherConstantValue(elem.getTextTrim());

                elem = shared.getChild("otherTableCell");
                if (elem != null) {
                    boolean result = false;
                    String ref = elem.getTextTrim();
                    if (!ref.isEmpty()) {
                        String[] refParts = ref.substring(1).split("[\\[\\]]");  // Remove first { and then split on [ and ]
//                      System.out.format("refParts.length: %d, '%s', '%s'%n", refParts.length, refParts[0], refParts[1]);
                        if (refParts.length == 3) {
                            String table = refParts[0];
                            String[] rowColumnParts = refParts[1].split(",");
                            if (rowColumnParts.length == 2) {
                                String row = rowColumnParts[0];
                                String column = rowColumnParts[1];
//                                System.out.format("Table: '%s', row: '%s', column: '%s'%n", table, row, column);

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

                elem = shared.getChild("otherVariable");
                if (elem != null) h.setOtherLocalVariable(elem.getTextTrim());

                elem = shared.getChild("otherFormula");
                if (elem != null) h.setOtherFormula(elem.getTextTrim());

            } catch (ParserException e) {
                throw new JmriConfigureXmlException(e);
            }
        }

        selectTableXml.load(shared.getChild("table"), h.getSelectTable());

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionMemoryXml.class);
}

package jmri.jmrit.logixng.util.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.util.LogixNG_SelectComboBox;
import jmri.jmrit.logixng.util.LogixNG_SelectComboBox.Item;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Xml class for jmri.jmrit.logixng.util.LogixNG_SelectComboBox.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class LogixNG_SelectComboBoxXml {

    /**
     * Default implementation for storing the contents of a LogixNG_SelectComboBox
     *
     * @param selectComboBox the LogixNG_SelectTable object
     * @param tagName the name of the element
     * @return Element containing the complete info
     */
    public Element store(LogixNG_SelectComboBox selectComboBox, String tagName) {

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        Element comboBoxElement = new Element(tagName);

        comboBoxElement.addContent(new Element("addressing").addContent(selectComboBox.getAddressing().name()));
        Item value = selectComboBox.getValue();
        if (value != null) {
            comboBoxElement.addContent(new Element("value").addContent(value.getKey()));
        }
        if (selectComboBox.getReference() != null && !selectComboBox.getReference().isEmpty()) {
            comboBoxElement.addContent(new Element("reference").addContent(selectComboBox.getReference()));
        }
        var memory = selectComboBox.getMemory();
        if (memory != null) {
            comboBoxElement.addContent(new Element("memory").addContent(memory.getName()));
        }
        comboBoxElement.addContent(new Element("listenToMemory").addContent(selectComboBox.getListenToMemory() ? "yes" : "no"));
        if (selectComboBox.getLocalVariable() != null && !selectComboBox.getLocalVariable().isEmpty()) {
            comboBoxElement.addContent(new Element("localVariable").addContent(selectComboBox.getLocalVariable()));
        }
        if (selectComboBox.getFormula() != null && !selectComboBox.getFormula().isEmpty()) {
            comboBoxElement.addContent(new Element("formula").addContent(selectComboBox.getFormula()));
        }

        if (selectComboBox.getAddressing() == NamedBeanAddressing.Table) {
            comboBoxElement.addContent(selectTableXml.store(selectComboBox.getSelectTable(), "table"));
        }

        return comboBoxElement;
    }

    public void load(Element comboBoxElement, LogixNG_SelectComboBox selectComboBox)
            throws JmriConfigureXmlException {

        if (comboBoxElement != null) {

            LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

            try {
                Element elem = comboBoxElement.getChild("addressing");
                if (elem != null) {
                    selectComboBox.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                elem = comboBoxElement.getChild("value");
                if (elem != null) {
                    selectComboBox.setValue(selectComboBox.getValueByKey(elem.getTextTrim()));
                }

                elem = comboBoxElement.getChild("reference");
                if (elem != null) selectComboBox.setReference(elem.getTextTrim());

                Element memoryName = comboBoxElement.getChild("memory");
                if (memoryName != null) {
                    Memory m = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
                    if (m != null) selectComboBox.setMemory(m);
                    else selectComboBox.removeMemory();
                }

                Element listenToMemoryElem = comboBoxElement.getChild("listenToMemory");
                if (listenToMemoryElem != null) {
                    selectComboBox.setListenToMemory("yes".equals(listenToMemoryElem.getTextTrim()));
                }

                elem = comboBoxElement.getChild("localVariable");
                if (elem != null) selectComboBox.setLocalVariable(elem.getTextTrim());

                elem = comboBoxElement.getChild("formula");
                if (elem != null) selectComboBox.setFormula(elem.getTextTrim());

                if (comboBoxElement.getChild("table") != null) {
                    selectTableXml.load(comboBoxElement.getChild("table"), selectComboBox.getSelectTable());
                }

            } catch (ParserException e) {
                throw new JmriConfigureXmlException(e);
            }
        }
    }

}

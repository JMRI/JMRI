package jmri.jmrit.logixng.util.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.util.LogixNG_SelectDouble;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Xml class for jmri.jmrit.logixng.util.LogixNG_SelectInteger.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectDoubleXml {

    /**
     * Default implementation for storing the contents of a LogixNG_SelectEnum
     *
     * @param selectDouble the LogixNG_SelectInteger object
     * @param tagName the name of the element
     * @return Element containing the complete info
     */
    public Element store(LogixNG_SelectDouble selectDouble, String tagName) {

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        Element enumElement = new Element(tagName);

        enumElement.addContent(new Element("addressing").addContent(selectDouble.getAddressing().name()));
        enumElement.addContent(new Element("value").addContent(selectDouble.formatValue(selectDouble.getValue())));
        enumElement.addContent(new Element("reference").addContent(selectDouble.getReference()));
        var memory = selectDouble.getMemory();
        if (memory != null) {
            enumElement.addContent(new Element("memory").addContent(memory.getName()));
        }
        enumElement.addContent(new Element("localVariable").addContent(selectDouble.getLocalVariable()));
        enumElement.addContent(new Element("formula").addContent(selectDouble.getFormula()));

        if (selectDouble.getAddressing() == NamedBeanAddressing.Table) {
            enumElement.addContent(selectTableXml.store(selectDouble.getSelectTable(), "table"));
        }

        return enumElement;
    }

    public void load(Element enumElement, LogixNG_SelectDouble selectDouble)
            throws JmriConfigureXmlException {

        if (enumElement != null) {

            LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

            try {
                Element elem = enumElement.getChild("addressing");
                if (elem != null) {
                    selectDouble.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                elem = enumElement.getChild("value");
                if (elem != null) {
                    selectDouble.setValue(Double.parseDouble(elem.getTextTrim()));
                }

                elem = enumElement.getChild("reference");
                if (elem != null) selectDouble.setReference(elem.getTextTrim());

                Element memoryName = enumElement.getChild("memory");
                if (memoryName != null) {
                    Memory m = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
                    if (m != null) selectDouble.setMemory(m);
                    else selectDouble.removeMemory();
                }

                elem = enumElement.getChild("localVariable");
                if (elem != null) selectDouble.setLocalVariable(elem.getTextTrim());

                elem = enumElement.getChild("formula");
                if (elem != null) selectDouble.setFormula(elem.getTextTrim());

                if (enumElement.getChild("table") != null) {
                    selectTableXml.load(enumElement.getChild("table"), selectDouble.getSelectTable());
                }

            } catch (ParserException e) {
                throw new JmriConfigureXmlException(e);
            }
        }
    }

}

package jmri.jmrit.logixng.util.configurexml;

import java.util.List;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.util.LogixNG_SelectStringList;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Xml class for jmri.jmrit.logixng.util.LogixNG_SelectStringList.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class LogixNG_SelectStringListXml {

    /**
     * Default implementation for storing the contents of a LogixNG_SelectEnum
     *
     * @param selectStr the LogixNG_SelectStringList object
     * @param tagName the name of the element
     * @return Element containing the complete info
     */
    public Element store(LogixNG_SelectStringList selectStr, String tagName) {

        Element element = new Element(tagName);

        element.addContent(new Element("addressing").addContent(selectStr.getAddressing().name()));
        Element listElement = new Element("values");
        List<String> list = selectStr.getList();
        if (list != null) {
            for (String s : list) {
                listElement.addContent(new Element("value").addContent(s));
            }
        }
        element.addContent(listElement);
        if (selectStr.getLocalVariable() != null && !selectStr.getLocalVariable().isEmpty()) {
            element.addContent(new Element("localVariable").addContent(selectStr.getLocalVariable()));
        }
        if (selectStr.getFormula() != null && !selectStr.getFormula().isEmpty()) {
            element.addContent(new Element("formula").addContent(selectStr.getFormula()));
        }

        return element;
    }

    public void load(Element strElement, LogixNG_SelectStringList selectStr)
            throws JmriConfigureXmlException {

        if (strElement != null) {

            try {
                Element elem = strElement.getChild("addressing");
                if (elem != null) {
                    selectStr.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                List<String> list = selectStr.getList();
                for (Element e : strElement.getChild("values").getChildren("value")) {
                    list.add(e.getTextTrim());
                }

                elem = strElement.getChild("localVariable");
                if (elem != null) selectStr.setLocalVariable(elem.getTextTrim());

                elem = strElement.getChild("formula");
                if (elem != null) selectStr.setFormula(elem.getTextTrim());

            } catch (ParserException e) {
                throw new JmriConfigureXmlException(e);
            }
        }
    }

}

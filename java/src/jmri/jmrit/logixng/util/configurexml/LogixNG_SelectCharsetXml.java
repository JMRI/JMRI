package jmri.jmrit.logixng.util.configurexml;

import java.nio.charset.Charset;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.util.LogixNG_SelectCharset;
import jmri.jmrit.logixng.util.LogixNG_SelectCharset.Addressing;

import org.jdom2.Element;

/**
 * Xml class for jmri.jmrit.logixng.util.LogixNG_SelectCharset.
 *
 * @author Daniel Bergqvist (C) 2023
 */
public class LogixNG_SelectCharsetXml {

    /**
     * Default implementation for storing the contents of a LogixNG_SelectEnum
     *
     * @param selectCharset the LogixNG_SelectCharset object
     * @param tagName       the name of the element
     * @return Element containing the complete info
     */
    public Element store(LogixNG_SelectCharset selectCharset, String tagName) {

        Element element = new Element(tagName);

        element.addContent(new Element("addressing").addContent(selectCharset.getAddressing().name()));
        if (selectCharset.getStandardValue() != null) {
            element.addContent(new Element("standardValue").addContent(selectCharset.getStandardValue().name()));
        }
        if (selectCharset.getAllValue() != null) {
            element.addContent(new Element("allValue").addContent(selectCharset.getAllValue().name()));
        }

        var selectUserSpecifiedXml = new LogixNG_SelectStringXml();
        element.addContent(selectUserSpecifiedXml.store(selectCharset.getSelectUserSpecified(), "userSpecified"));

        return element;
    }

    public void load(Element strElement, LogixNG_SelectCharset selectCharset)
            throws JmriConfigureXmlException {

        if (strElement != null) {

            Element elem = strElement.getChild("addressing");
            if (elem != null) {
                selectCharset.setAddressing(Addressing.valueOf(elem.getTextTrim()));
            }

            elem = strElement.getChild("standardValue");
            if (elem != null) {
                selectCharset.setStandardValue(Charset.forName(elem.getTextTrim()));
            }

            elem = strElement.getChild("allValue");
            if (elem != null) {
                selectCharset.setAllValue(Charset.forName(elem.getTextTrim()));
            }

            var selectUserSpecifiedXml = new LogixNG_SelectStringXml();
            if (strElement.getChild("table") != null) {
                selectUserSpecifiedXml.load(strElement.getChild("userSpecified"),
                        selectCharset.getSelectUserSpecified());
            }
        }
    }

}

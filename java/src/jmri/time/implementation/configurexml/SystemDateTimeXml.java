package jmri.time.implementation.configurexml;

import jmri.time.implementation.SystemDateTime;

import org.jdom2.Element;

/**
 * Store and load an SystemDateTime.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class SystemDateTimeXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    @Override
    public Element store(Object o) {
        SystemDateTime p = (SystemDateTime) o;

        Element element = new Element("InternalDateTime");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        return element;
    }

}

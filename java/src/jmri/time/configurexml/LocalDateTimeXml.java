package jmri.time.configurexml;

import java.time.LocalDateTime;

import org.jdom2.Element;

/**
 * Store and load an LocalDateTime.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class LocalDateTimeXml {

    public static void store(LocalDateTime localDateTime, Element element) {
        element.addContent(new Element("localDateTime").addContent(localDateTime.toString()));
    }

    public static LocalDateTime load(Element element) {
        return null;
    }

}

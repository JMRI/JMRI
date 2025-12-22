package jmri.time.configurexml;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import javax.annotation.Nonnull;

import jmri.time.TimeSetter;

import org.jdom2.Element;

/**
 * Store and load an LocalDateTime.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class LocalDateTimeXml {

    public static void store(@Nonnull LocalDateTime localDateTime, @Nonnull Element element) {
        element.addContent(new Element("localDateTime").addContent(localDateTime.toString()));
    }

    public static boolean load(Element element, TimeSetter ts) {
        Element elem = element.getChild("localDateTime");
        if (elem != null) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(elem.getTextTrim());
                ts.setDateTime(ldt);
            } catch (DateTimeParseException e) {
                log.error("DateTime is not a LocalDateTime: {}", elem.getTextTrim());
                return false;
            } catch (UnsupportedOperationException e) {
                log.error("This time provider cannot set date and time: {}", ts.getClass().getName());
                return false;
            }
        }
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LocalDateTimeXml.class);
}

package jmri.time.configurexml;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import javax.annotation.Nonnull;

import jmri.time.TimeSetter;

import org.jdom2.Element;

/**
 * Store and load an ZonedDateTime.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class ZonedDateTimeXml {

    public static void store(@Nonnull ZonedDateTime zonedDateTime, @Nonnull Element element) {
        element.addContent(new Element("zonedDateTime").addContent(zonedDateTime.toString()));
    }

    public static boolean load(Element element, TimeSetter ts) {
        Element elem = element.getChild("zonedDateTime");
        if (elem != null) {
            try {
                ZonedDateTime ldt = ZonedDateTime.parse(elem.getTextTrim());
                ts.setDateTime(ldt);
            } catch (DateTimeParseException e) {
                log.error("DateTime is not a ZonedDateTime: {}", elem.getTextTrim());
                return false;
            } catch (UnsupportedOperationException e) {
                log.error("This time provider cannot set date and time: {}", ts.getClass().getName());
                return false;
            }
        }
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ZonedDateTimeXml.class);
}

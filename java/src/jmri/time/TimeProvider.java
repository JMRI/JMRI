package jmri.time;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import jmri.NamedBean;

/**
 * A time provider.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public interface TimeProvider extends NamedBean {

    /**
     * The format of date and time used in XML files.
     */
    public static final DateTimeFormatter LEGACY_XML_DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy");
//            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss v yyyy");

    /**
     * Property Change sent when the minute value changes.
     * The values are the number of seconds since epoc.
     */
    String PROPERTY_CHANGE_SECONDS = "seconds";

    /**
     * Property Change sent when the minute value changes.
     * The values are minute of the hour.
     */
    String PROPERTY_CHANGE_MINUTES = "minutes";

    /**
     * Property Change sent when the rate value changes.
     */
    String PROPERTY_CHANGE_RATE = "rate";

    /**
     * Property Change sent when the run status changes.
     */
    String PROPERTY_CHANGE_RUN = "run";

    /**
     * Property Change sent when the minute value changes.
     * The values are {@link ZonedDateTime}.
     */
    String PROPERTY_CHANGE_DATETIME = "datetime";

    /**
     * Get the time object of this time provider. If the time provider implements the
     * {@code DateSupport} interface, this method will also return some kind
     * of date.
     * @return the time and possibly date.
     */
    ZonedDateTime getTime();

    /**
     * Get the rate object of this time provider.
     * @return the Rate object.
     */
    Rate getRate();

    /**
     * Is the time provider running?
     * @return true if running, false if stopped.
     */
    boolean isRunning();

}

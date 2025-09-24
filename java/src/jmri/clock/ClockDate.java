package jmri.clock;

import java.util.Calendar;

import javax.annotation.Nonnull;

/**
 * The date of a clock.
 *
 * @author Bob Jacobsen      Copyright (C) 2004, 2007, 2008
 * @author Daniel Bergqvist  Copyright (C) 2025
 */
public interface ClockDate extends ClockTime {

    /**
     * Property Change sent when the minute value changes.
     */
    String PROPERTY_CHANGE_DATE = "date";

    /**
     * Set the current date.
     *
     * @param d the new date
     */
    void setDate(@Nonnull Calendar d);

    /**
     * Set the current date and force a synchronization with the DCC system.
     *
     * @param date the new date
     */
    void userSetDate(@Nonnull Calendar date);

    /**
     * Get the current date.
     * @return current date.
     */
    @Nonnull
    Calendar getDate();

}

package jmri.clock;

/**
 * The display of a clock.
 *
 * @author Bob Jacobsen      Copyright (C) 2004, 2007, 2008
 * @author Daniel Bergqvist  Copyright (C) 2025
 */
public interface ClockDisplay {

    /**
     * Set 12 or 24 hour display option.
     *
     * @param display true for a 12-hour display; false for a 24-hour display
     * @param update true to update clock when function called.
     */
    void set12HourDisplay(boolean display, boolean update);

    /**
     * Get 12 or 24 hour display option.
     *
     * @return true for a 12-hour display; false for a 24-hour display
     */
    boolean use12HourDisplay();

    /**
     * Set if to show a Stop / Resume button next to the clock.
     *
     * @param displayed true if to display, else false.
     */
    void setShowStopButton(boolean displayed);

    /**
     * Get if to show a Stop / Resume button next to the clock.
     *
     * @return true if to display, else false.
     */
    boolean getShowStopButton();

}

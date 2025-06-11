package jmri.time;

/**
 * Set the date of the time provider.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public interface DateSetter {

    /**
     * Set the weekday of the time provider.
     * @param dayOfWeek the day of week. 1 = Monday, 7 = Sunday
     * @throws UnsupportedOperationException if the time couldn't be set
     */
    void setWeekday(int dayOfWeek) throws UnsupportedOperationException;

    /**
     * Set the day the month of the time provider.
     * @param day the day
     * @throws UnsupportedOperationException if the time couldn't be set
     */
    void setDayOfMonth(int day) throws UnsupportedOperationException;

    /**
     * Set the month of the time provider.
     * @param month the month
     * @throws UnsupportedOperationException if the time couldn't be set
     */
    void setMonth(int month) throws UnsupportedOperationException;

    /**
     * Set the year of the time provider.
     * @param year the year
     * @throws UnsupportedOperationException if the time couldn't be set
     */
    void setYear(int year) throws UnsupportedOperationException;

    /**
     * Can the date be set?
     * @return true if the date can be set, false otherwise
     */
    boolean canSetDate();

}

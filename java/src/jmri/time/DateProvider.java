package jmri.time;

/**
 * A {@link TimeProvider} that implements this interface supports some kind
 * of date.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public interface DateProvider {

    /**
     * Does this time provider has weekday?
     * @return true if the time provider has weekday
     */
    boolean hasWeekday();

    /**
     * Does this time provider has day of month?
     * @return true if the time provider has day of month
     */
    boolean hasDayOfMonth();

    /**
     * Does this time provider has month?
     * @return true if the time provider has month
     */
    boolean hasMonth();

    /**
     * Does this time provider has year?
     * @return true if the time provider has year
     */
    boolean hasYear();

}

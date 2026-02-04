package jmri.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Common utility methods for working with Calendar and Date objects.
 *
 * @author Paul Bender Copyright 2014
 */
public class DateUtil {

    // class only supplies static methods
    private DateUtil(){}

    /**
     * Return a GregorianCalendar representation of the given julian date.
     * For reference, see: http://aa.usno.navy.mil/faq/docs/JD_Formula.php
     * @param julianDay number of days since January 1,4713BC.
     * @return {@link java.util.GregorianCalendar} representation of julianDay
     */
    public static GregorianCalendar calFromJulianDate(long julianDay) {
        long l = julianDay + 68569;
        long n = 4 * l / 146097;
        l -= (146097 * n + 3) / 4;
        long year = 4000 * (l + 1) / 1461001;
        l = l - 1461 * year / 4 + 31;
        long month = 80 * l / 2447;
        long day = l - 2447 * (month) / 80 - 31;
        l = month / 11;
        month = month + 2 - 12 * l;
        year = 100 * (n - 49) + year + l;
        return  new GregorianCalendar((int) year, (int) month, (int) day, 0, 0);
    }

    /**
     * Return a julian date representation of the given GregorianCalendar date.
     * for reference, see: http://aa.usno.navy.mil/faq/docs/JD_Formula.php
     * @param cal the GregorianCalendar to convert.
     * @return julianDate representation of the date represented by cal.
     */
    public static long julianDayFromCalendar(java.util.GregorianCalendar cal) {
        int i = cal.get(Calendar.YEAR);
        int j = cal.get(Calendar.MONTH) + 1; // GregorianCalendar starts month
        // at 0.  Calculation requres month
        // starting at 1.

        int k = cal.get(Calendar.DAY_OF_MONTH);

        return k - 32075 + 1461 * (i + 4800 + (j - 14) / 12) / 4 +
            367 * (j - 2 - (j - 14) / 12 * 12) / 12 - 3 * ((i + 4900 + (j - 14) / 12) / 100) / 4;
    }

    /**
     * For a given number of seconds, format to a more human readable form.
     * Negative durations are prepended by the minus symbol.
     * For durations less than 24hrs, the day integer is omitted.
     * @param seconds the number of seconds
     * @return string representation of duration in D hh:mm:ss form.
     */
    public static String userDurationFromSeconds(int seconds) {
        String minus = ( seconds < 0 ? "- " : "" );
        var duration = java.time.Duration.ofSeconds(Math.abs(seconds));
        long days = duration.toDays();
        long hours = duration.minusDays(days).toHours();
        long mins = duration.minusDays(days).minusHours(hours).toMinutes();
        long secs = duration.minusDays(days).minusHours(hours).minusMinutes(mins).toSeconds();
        if ( days == 0 ) {
            return minus + String.format("%02d:%02d:%02d", hours, mins, secs);
        }
        else {
            return minus + String.format("%d %02d:%02d:%02d", days, hours, mins, secs);
        }
    }

}
